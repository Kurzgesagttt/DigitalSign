package com.Kurzgesagtt.DigitalSignature;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.Kurzgesagtt.DigitalSignature.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthenticationValidationIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository users;
    @Autowired SignatureRecordRepository signatures;
    @Autowired AuditLogRepository audits;

    @BeforeEach
    void clean() {
        audits.deleteAll();
        signatures.deleteAll();
        users.deleteAll();
    }

    @ParameterizedTest
    @MethodSource("invalidFields")
    void registrationRejectsInvalidOrUnboundedProfileData(Map<String, String> override) throws Exception {
        Map<String, String> body = new HashMap<>(Map.of(
                "nomeCompleto", "Pessoa Sintetica", "email", "validation@example.test",
                "telefone", "11999990000", "cpf", "11122233344",
                "dataNascimento", "1990-01-01", "senha", "synthetic-password"));
        body.putAll(override);
        mvc.perform(post("/v1/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    static Stream<Map<String, String>> invalidFields() {
        return Stream.of(Map.of("email", "invalid-email"), Map.of("telefone", "abc"),
                Map.of("dataNascimento", "2990-01-01"), Map.of("nomeCompleto", "x".repeat(256)),
                Map.of("senha", "short"), Map.of("senha", "x".repeat(73)), Map.of("senha", "é".repeat(40)));
    }
}
