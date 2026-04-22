package com.Kurzgesagtt.DigitalSignature.controller;

import com.Kurzgesagtt.DigitalSignature.model.Usuario;
import com.Kurzgesagtt.DigitalSignature.repository.UsuarioRepository;
import com.Kurzgesagtt.DigitalSignature.security.LoginRateLimiter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private LoginRateLimiter loginRateLimiter;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        if (request.nomeCompleto == null || request.nomeCompleto.isBlank()
                || request.email == null || request.email.isBlank()
                || request.telefone == null || request.telefone.isBlank()
                || request.cpf == null || request.cpf.isBlank()
                || request.dataNascimento == null
                || request.senha == null || request.senha.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Todos os campos são obrigatórios."));
        }

        if (!Boolean.TRUE.equals(request.consentimento)) {
            return ResponseEntity.badRequest().body(Map.of("erro", "É necessário aceitar os termos de uso e a política de privacidade (LGPD)."));
        }

        String cpfLimpo = request.cpf.replaceAll("\\D", "");
        if (cpfLimpo.length() != 11) {
            return ResponseEntity.badRequest().body(Map.of("erro", "CPF inválido."));
        }

        if (usuarioRepository.existsByEmail(request.email.trim().toLowerCase())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "Email já cadastrado."));
        }
        if (usuarioRepository.existsByCpf(cpfLimpo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "CPF já cadastrado."));
        }

        LocalDateTime agora = LocalDateTime.now();

        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(request.nomeCompleto.trim());
        usuario.setEmail(request.email.trim().toLowerCase());
        usuario.setTelefone(request.telefone.replaceAll("\\D", ""));
        usuario.setCpf(cpfLimpo);
        usuario.setDataNascimento(request.dataNascimento);
        usuario.setSenha(passwordEncoder.encode(request.senha));
        usuario.setCriadoEm(agora);
        usuario.setConsentimentoLGPD(true);
        usuario.setConsentimentoEm(agora);

        usuarioRepository.save(usuario);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "mensagem", "Usuário cadastrado com sucesso.",
                "id", usuario.getId(),
                "nome", usuario.getNomeCompleto(),
                "email", usuario.getEmail()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (request.email == null || request.email.isBlank()
                || request.senha == null || request.senha.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Email e senha são obrigatórios."));
        }

        String emailNormalizado = request.email.trim().toLowerCase();

        if (loginRateLimiter.isBlocked(emailNormalizado)) {
            long segundos = loginRateLimiter.segundosRestantes(emailNormalizado);
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(
                    Map.of("erro", "Muitas tentativas de login. Tente novamente em " + segundos + " segundos.")
            );
        }

        var optUsuario = usuarioRepository.findByEmail(emailNormalizado);
        if (optUsuario.isEmpty()) {
            loginRateLimiter.registrarFalha(emailNormalizado);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Email ou senha incorretos."));
        }

        Usuario usuario = optUsuario.get();

        if (!usuario.isAtivo()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Conta desativada. Entre em contato com o suporte."));
        }

        if (!passwordEncoder.matches(request.senha, usuario.getSenha())) {
            loginRateLimiter.registrarFalha(emailNormalizado);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Email ou senha incorretos."));
        }

        loginRateLimiter.registrarSucesso(emailNormalizado);

        return ResponseEntity.ok(Map.of(
                "mensagem", "Login realizado com sucesso.",
                "id", usuario.getId(),
                "nome", usuario.getNomeCompleto(),
                "email", usuario.getEmail(),
                "cpf", usuario.getCpf()
        ));
    }

    public static class RegisterRequest {
        public String nomeCompleto;
        public String email;
        public String telefone;
        public String cpf;
        public LocalDate dataNascimento;
        public String senha;
        public Boolean consentimento;
    }

    public static class LoginRequest {
        public String email;
        public String senha;
    }
}
