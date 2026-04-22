package com.Kurzgesagtt.DigitalSignature.controller;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Kurzgesagtt.DigitalSignature.model.Usuario;
import com.Kurzgesagtt.DigitalSignature.repository.UsuarioRepository;

/**
 * Endpoints de privacidade conforme LGPD (Lei 13.709/2018):
 * - Art. 18 inciso II  → acesso aos dados (exportação)
 * - Art. 18 inciso VI  → exclusão dos dados (direito ao esquecimento)
 *
 * Todos os endpoints exigem autenticação via email + senha do próprio titular.
 */
@RestController
@RequestMapping("/v1/privacy")
public class LgpdController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public static class AuthRequest {
        public String email;
        public String senha;
    }

    /**
     * Autentica o titular e retorna o usuário, ou null se inválido.
     * Garante que só o próprio titular acessa seus dados.
     */
    private Usuario autenticar(AuthRequest auth, Long userId) {
        if (auth == null || auth.email == null || auth.email.isBlank()
                || auth.senha == null || auth.senha.isBlank()) {
            return null;
        }
        Optional<Usuario> opt = usuarioRepository.findByEmail(auth.email.trim().toLowerCase());
        if (opt.isEmpty()) return null;
        Usuario u = opt.get();
        if (!u.getId().equals(userId)) return null;
        if (!passwordEncoder.matches(auth.senha, u.getSenha())) return null;
        return u;
    }

    /**
     * LGPD Art. 18 inciso II — Acesso e portabilidade dos dados pessoais.
     * Requer autenticação: o titular deve fornecer seu email e senha no corpo da requisição.
     */
    @PostMapping("/export/{userId}")
    public ResponseEntity<?> exportarDados(@PathVariable Long userId, @RequestBody AuthRequest auth) {
        Usuario u = autenticar(auth, userId);
        if (u == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Credenciais inválidas ou não correspondem a este usuário."));
        }

        if (!u.isAtivo()) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("erro", "Esta conta foi encerrada e os dados foram anonimizados."));
        }

        Map<String, Object> dados = new LinkedHashMap<>();
        dados.put("id", u.getId());
        dados.put("nomeCompleto", u.getNomeCompleto());
        dados.put("email", u.getEmail());
        dados.put("telefone", u.getTelefone());
        dados.put("cpf", u.getCpf());
        dados.put("dataNascimento", u.getDataNascimento() != null ? u.getDataNascimento().toString() : null);
        dados.put("criadoEm", u.getCriadoEm() != null ? u.getCriadoEm().format(FORMATTER) : null);
        dados.put("consentimentoLGPD", u.isConsentimentoLGPD());
        dados.put("consentimentoEm", u.getConsentimentoEm() != null ? u.getConsentimentoEm().format(FORMATTER) : null);
        dados.put("aviso", "Estes são todos os dados pessoais armazenados em nosso sistema sobre você, conforme LGPD Art. 18 inciso II.");

        return ResponseEntity.ok(dados);
    }

    /**
     * LGPD Art. 18 inciso VI — Direito ao esquecimento / exclusão.
     * Requer autenticação: o titular deve fornecer seu email e senha no corpo da requisição.
     * Realiza soft-delete: anonimiza os dados pessoais e marca a conta como excluída.
     * Registros de assinatura são mantidos por obrigação legal (Art. 16 LGPD).
     */
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> excluirConta(@PathVariable Long userId, @RequestBody AuthRequest auth) {
        Usuario u = autenticar(auth, userId);
        if (u == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("erro", "Credenciais inválidas ou não correspondem a este usuário."));
        }

        if (!u.isAtivo()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("erro", "Conta já encerrada anteriormente."));
        }

        // Anonimização dos dados pessoais (LGPD Art. 12 — dado anonimizado)
        u.setNomeCompleto("Usuário Removido");
        u.setEmail("removido_" + userId + "@anonimizado.invalid");
        u.setTelefone("00000000000");
        u.setCpf("00000000000");
        u.setDeletadoEm(LocalDateTime.now());

        usuarioRepository.save(u);

        return ResponseEntity.ok(Map.of(
                "mensagem", "Conta encerrada com sucesso. Seus dados pessoais foram anonimizados conforme a LGPD Art. 18 inciso VI.",
                "aviso", "Registros de assinatura digital são mantidos por obrigação legal (LGPD Art. 16) e não podem ser excluídos."
        ));
    }
}

