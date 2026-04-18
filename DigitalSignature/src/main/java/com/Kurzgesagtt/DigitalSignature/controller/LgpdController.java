package com.Kurzgesagtt.DigitalSignature.controller;

import com.Kurzgesagtt.DigitalSignature.model.Usuario;
import com.Kurzgesagtt.DigitalSignature.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Endpoints de privacidade conforme LGPD (Lei 13.709/2018):
 * - Art. 18 inciso II  → acesso aos dados (exportação)
 * - Art. 18 inciso VI  → exclusão dos dados (direito ao esquecimento)
 */
@RestController
@RequestMapping("/v1/privacy")
public class LgpdController {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @Autowired
    private UsuarioRepository usuarioRepository;

    /**
     * LGPD Art. 18 inciso II — Acesso e portabilidade dos dados pessoais.
     * Retorna todos os dados armazenados sobre o titular.
     */
    @GetMapping("/export/{userId}")
    public ResponseEntity<?> exportarDados(@PathVariable Long userId) {
        Optional<Usuario> opt = usuarioRepository.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Usuário não encontrado."));
        }

        Usuario u = opt.get();

        if (!u.isAtivo()) {
            return ResponseEntity.status(HttpStatus.GONE).body(Map.of("erro", "Esta conta foi encerrada e os dados foram anonimizados."));
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
     * Realiza soft-delete: anonimiza os dados pessoais e marca a conta como excluída.
     * Registros de assinatura são mantidos por obrigação legal (Art. 16 LGPD).
     */
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<?> excluirConta(@PathVariable Long userId) {
        Optional<Usuario> opt = usuarioRepository.findById(userId);
        if (opt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("erro", "Usuário não encontrado."));
        }

        Usuario u = opt.get();

        if (!u.isAtivo()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "Conta já encerrada anteriormente."));
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
