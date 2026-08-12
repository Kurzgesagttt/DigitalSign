package com.Kurzgesagtt.DigitalSignature;

import com.Kurzgesagtt.DigitalSignature.model.SignatureRecord;
import com.Kurzgesagtt.DigitalSignature.model.SignatureStatus;
import com.Kurzgesagtt.DigitalSignature.model.UserRole;
import com.Kurzgesagtt.DigitalSignature.model.Usuario;
import com.Kurzgesagtt.DigitalSignature.repository.AuditLogRepository;
import com.Kurzgesagtt.DigitalSignature.repository.SignatureRecordRepository;
import com.Kurzgesagtt.DigitalSignature.repository.UsuarioRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DigitalSignatureApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UsuarioRepository usuarioRepository;

    @Autowired
    SignatureRecordRepository signatureRecordRepository;

    @Autowired
    AuditLogRepository auditLogRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void cleanDatabase() {
        auditLogRepository.deleteAll();
        signatureRecordRepository.deleteAll();
        usuarioRepository.deleteAll();
    }

    @Test
    void registerCreatesUserRoleBcryptPasswordAndToken() throws Exception {
        String body = objectMapper.writeValueAsString(Map.of(
                "nomeCompleto", "Lucas Admin",
                "email", "lucas@example.com",
                "telefone", "11999998888",
                "cpf", "12345678901",
                "dataNascimento", "1990-01-01",
                "senha", "secret123"
        ));

        mockMvc.perform(post("/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.role").value("USER"))
                .andExpect(jsonPath("$.token").isString());

        Usuario saved = usuarioRepository.findByEmail("lucas@example.com").orElseThrow();
        assertThat(saved.getRole()).isEqualTo(UserRole.USER);
        assertThat(saved.getSenha()).startsWith("$2");
        assertThat(passwordEncoder.matches("secret123", saved.getSenha())).isTrue();
    }

    @Test
    void protectedEndpointsRequireAuthAndAdminRole() throws Exception {
        Usuario user = createUser("user@example.com", "12345678901", UserRole.USER);
        String userToken = login("user@example.com", "secret123");

        mockMvc.perform(post("/v1/sign"))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/v1/admin/users")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());

        Usuario admin = createUser("admin@example.com", "98765432100", UserRole.ADMIN);
        String adminToken = login(admin.getEmail(), "secret123");

        mockMvc.perform(get("/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());

        assertThat(user.getRole()).isEqualTo(UserRole.USER);
    }

    @Test
    void adminCanBlockUserAndBlockedUserCannotLogin() throws Exception {
        Usuario admin = createUser("admin@example.com", "98765432100", UserRole.ADMIN);
        Usuario user = createUser("user@example.com", "12345678901", UserRole.USER);
        String adminToken = login(admin.getEmail(), "secret123");

        mockMvc.perform(patch("/v1/admin/users/" + user.getId() + "/block")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("motivo", "uso indevido"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bloqueado").value(true));

        mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", user.getEmail(),
                                "senha", "secret123"
                        ))))
                .andExpect(status().isForbidden());
    }

    @Test
    void authenticatedSignUsesLoggedUserAndStoresMetadata() throws Exception {
        Usuario user = createUser("signer@example.com", "12345678901", UserRole.USER);
        String token = login(user.getEmail(), "secret123");
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "documento.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                samplePdf()
        );

        mockMvc.perform(multipart("/v1/sign")
                        .file(file)
                        .header("Authorization", "Bearer " + token)
                        .header("User-Agent", "JUnit")
                        .with(request -> {
                            request.setRemoteAddr("127.0.0.1");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-Verification-Code"))
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));

        SignatureRecord record = signatureRecordRepository.findAll().get(0);
        assertThat(record.getUsuario().getId()).isEqualTo(user.getId());
        assertThat(record.getSignatarioEmail()).isEqualTo(user.getEmail());
        assertThat(record.getStatus()).isEqualTo(SignatureStatus.VALIDA);
        assertThat(record.getOriginalPdfHash()).hasSize(64);
        assertThat(record.getSignedPdfHash()).hasSize(64);
    }

    @Test
    void revokedSignatureIsPubliclyReportedAsInvalid() throws Exception {
        Usuario user = createUser("signer@example.com", "12345678901", UserRole.USER);
        SignatureRecord record = new SignatureRecord();
        record.setVerificationCode("abc12345");
        record.setUsuario(user);
        record.setSignatarioNome(user.getNomeCompleto());
        record.setSignatarioEmail(user.getEmail());
        record.setSignatarioCpf(user.getCpf());
        record.setAssinadoEm(LocalDateTime.now());
        record.setStatus(SignatureStatus.REVOGADA);
        record.setRevogacaoMotivo("documento cancelado");
        signatureRecordRepository.save(record);

        mockMvc.perform(get("/v1/verify/abc12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valido").value(false))
                .andExpect(jsonPath("$.status").value("REVOGADA"));
    }

    private Usuario createUser(String email, String cpf, UserRole role) {
        Usuario usuario = new Usuario();
        usuario.setNomeCompleto("Usuario Teste");
        usuario.setEmail(email);
        usuario.setTelefone("11999998888");
        usuario.setCpf(cpf);
        usuario.setDataNascimento(LocalDate.of(1990, 1, 1));
        usuario.setSenha(passwordEncoder.encode("secret123"));
        usuario.setRole(role);
        return usuarioRepository.save(usuario);
    }

    private String login(String email, String senha) throws Exception {
        String response = mockMvc.perform(post("/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", email,
                                "senha", senha
                        ))))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();
        JsonNode json = objectMapper.readTree(response);
        return json.get("token").asText();
    }

    private byte[] samplePdf() throws Exception {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            document.addPage(new PDPage());
            document.save(output);
            return output.toByteArray();
        }
    }
}
