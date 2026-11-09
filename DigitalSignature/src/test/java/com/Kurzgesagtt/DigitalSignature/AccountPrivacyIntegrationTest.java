package com.Kurzgesagtt.DigitalSignature;

import com.Kurzgesagtt.DigitalSignature.model.*;
import com.Kurzgesagtt.DigitalSignature.repository.*;
import com.Kurzgesagtt.DigitalSignature.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AccountPrivacyIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository users;
    @Autowired SignatureRecordRepository signatures;
    @Autowired AuditLogRepository audits;
    @Autowired PasswordEncoder passwords;
    @Autowired JwtService jwt;
    @Autowired JdbcTemplate jdbc;

    @BeforeEach
    void clean() {
        audits.deleteAll();
        signatures.deleteAll();
        users.deleteAll();
    }

    @Test
    void profileOnlyReturnsAuthenticatedAccountAndDescribesStoredData() throws Exception {
        Usuario user = user("owner@example.test", "11122233344");
        user("other@example.test", "55566677788");
        signature(user, "own-signature");
        mvc.perform(get("/v1/users/me").header("Authorization", "Bearer " + jwt.generateToken(user)))
                .andExpect(status().isOk())
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(jsonPath("$.email").value(user.getEmail()))
                .andExpect(jsonPath("$.cpf").value(user.getCpf()))
                .andExpect(jsonPath("$.totalAssinaturas").value(1))
                .andExpect(jsonPath("$.categoriasDados").isArray())
                .andExpect(jsonPath("$.exclusao.limitacoes").isArray())
                .andExpect(jsonPath("$.senha").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        mvc.perform(get("/v1/users/me")).andExpect(status().isForbidden());
    }

    @Test
    void deletionRequiresCurrentPasswordAndDoesNotAcceptTargetAccountId() throws Exception {
        Usuario user = user("owner@example.test", "11122233344");
        Usuario other = user("other@example.test", "55566677788");
        String token = jwt.generateToken(user);
        mvc.perform(delete("/v1/users/me")).andExpect(status().isForbidden());
        mvc.perform(delete("/v1/users/me").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(delete("/v1/users/me").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"senha\":\"wrong-password\"}"))
                .andExpect(status().isUnauthorized());
        assertThat(users.existsById(user.getId())).isTrue();
        mvc.perform(delete("/v1/users/me").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(Map.of("senha", "synthetic-password", "id", other.getId()))))
                .andExpect(status().isNoContent());
        assertThat(users.existsById(user.getId())).isFalse();
        assertThat(users.existsById(other.getId())).isTrue();
    }

    @Test
    void deletionRemovesPersonalReferencesAndVerificationWithoutRemovingOtherAccounts() throws Exception {
        Usuario owner = user("owner@example.test", "11122233344");
        Usuario other = user("other@example.test", "55566677788");
        other.setBloqueadoPorUsuarioId(owner.getId());
        users.save(other);
        SignatureRecord own = signature(owner, "deleted-code");
        SignatureRecord unrelated = signature(other, "surviving-code");
        unrelated.setRevogadoPorUsuarioId(owner.getId());
        signatures.save(unrelated);
        audit(owner, "USUARIO", owner.getId());
        audit(null, "SIGNATURE", own.getId());
        audit(other, "USUARIO", owner.getId());
        audit(other, "SIGNATURE", unrelated.getId());

        mvc.perform(delete("/v1/users/me").header("Authorization", "Bearer " + jwt.generateToken(owner))
                        .contentType(MediaType.APPLICATION_JSON).content("{\"senha\":\"synthetic-password\"}"))
                .andExpect(status().isNoContent());
        assertThat(users.count()).isEqualTo(1);
        assertThat(signatures.count()).isEqualTo(1);
        assertThat(audits.count()).isEqualTo(1);
        assertThat(users.findById(other.getId()).orElseThrow().getBloqueadoPorUsuarioId()).isNull();
        assertThat(signatures.findById(unrelated.getId()).orElseThrow().getRevogadoPorUsuarioId()).isNull();
        mvc.perform(get("/v1/verify/deleted-code")).andExpect(jsonPath("$.valido").value(false));
        mvc.perform(get("/v1/verify/surviving-code")).andExpect(jsonPath("$.valido").value(true));
    }

    @Test
    void deletedAccountsTokenCannotAuthenticateAReplacementWithTheSameEmail() throws Exception {
        Usuario original = user("reused@example.test", "11122233344");
        String oldToken = jwt.generateToken(original);
        users.delete(original);
        Usuario replacement = user("reused@example.test", "55566677788");
        mvc.perform(get("/v1/admin/users").header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isForbidden());
        // Signing already exists: a valid token would pass authentication and return its instructions.
        mvc.perform(get("/v1/sign").header("Authorization", "Bearer " + oldToken))
                .andExpect(status().isForbidden());
        mvc.perform(get("/v1/sign").header("Authorization", "Bearer " + jwt.generateToken(replacement)))
                .andExpect(status().isOk());
    }

    @Test
    void browserCanPreflightAuthenticatedDeletion() throws Exception {
        mvc.perform(options("/v1/users/me").header("Origin", "http://localhost:5173")
                        .header("Access-Control-Request-Method", "DELETE")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Methods", org.hamcrest.Matchers.containsString("DELETE")));
    }

    @Test
    void failedDeletionRollsBackSignatureAndAuditRemoval() throws Exception {
        Usuario owner = user("rollback@example.test", "11122233344");
        SignatureRecord signature = signature(owner, "rollback-code");
        audit(owner, "SIGNATURE", signature.getId());
        // A real database constraint causes the final account delete to fail after child-row deletion.
        jdbc.execute("create table account_deletion_guard (user_id bigint references usuarios(id))");
        try {
            jdbc.update("insert into account_deletion_guard(user_id) values (?)", owner.getId());
            mvc.perform(delete("/v1/users/me").header("Authorization", "Bearer " + jwt.generateToken(owner))
                            .contentType(MediaType.APPLICATION_JSON).content("{\"senha\":\"synthetic-password\"}"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.erro").exists())
                    .andExpect(jsonPath("$.trace").doesNotExist());
            assertThat(users.existsById(owner.getId())).isTrue();
            assertThat(signatures.existsById(signature.getId())).isTrue();
            assertThat(audits.count()).isEqualTo(1);
        } finally {
            jdbc.execute("drop table account_deletion_guard");
        }
    }

    private Usuario user(String email, String cpf) {
        Usuario user = new Usuario();
        user.setNomeCompleto("Pessoa de Teste");
        user.setEmail(email);
        user.setCpf(cpf);
        user.setTelefone("11999990000");
        user.setDataNascimento(LocalDate.of(1990, 1, 1));
        user.setSenha(passwords.encode("synthetic-password"));
        return users.save(user);
    }

    private SignatureRecord signature(Usuario user, String code) {
        SignatureRecord signature = new SignatureRecord();
        signature.setUsuario(user);
        signature.setVerificationCode(code);
        signature.setSignatarioNome(user.getNomeCompleto());
        signature.setSignatarioEmail(user.getEmail());
        signature.setSignatarioCpf(user.getCpf());
        return signatures.save(signature);
    }

    private void audit(Usuario actor, String type, Long targetId) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        if (actor != null) log.setActorEmail(actor.getEmail());
        log.setTargetType(type);
        log.setTargetId(targetId);
        log.setEventType("SYNTHETIC_TEST");
        log.setResult("SUCCESS");
        audits.save(log);
    }
}
