package com.Kurzgesagtt.DigitalSignature.controller;

import com.Kurzgesagtt.DigitalSignature.model.UserRole;
import com.Kurzgesagtt.DigitalSignature.model.Usuario;
import com.Kurzgesagtt.DigitalSignature.repository.UsuarioRepository;
import com.Kurzgesagtt.DigitalSignature.services.AuditService;
import com.Kurzgesagtt.DigitalSignature.services.JwtService;
import com.Kurzgesagtt.DigitalSignature.services.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HexFormat;
import java.util.Map;
import java.util.Locale;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;

    public AuthController(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            AuditService auditService,
            RateLimitService rateLimitService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.auditService = auditService;
        this.rateLimitService = rateLimitService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        if (request.nomeCompleto == null || request.nomeCompleto.isBlank()
                || request.email == null || request.email.isBlank()
                || request.telefone == null || request.telefone.isBlank()
                || request.cpf == null || request.cpf.isBlank()
                || request.dataNascimento == null
                || request.senha == null || request.senha.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Todos os campos sao obrigatorios."));
        }

        String cpfLimpo = request.cpf.replaceAll("\\D", "");
        if (cpfLimpo.length() != 11) {
            return ResponseEntity.badRequest().body(Map.of("erro", "CPF invalido."));
        }

        String telefoneLimpo = request.telefone.replaceAll("\\D", "");
        if (telefoneLimpo.length() < 10 || telefoneLimpo.length() > 11) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Telefone invalido."));
        }
        if (request.senha.getBytes(StandardCharsets.UTF_8).length > 72) {
            return ResponseEntity.badRequest().body(Map.of("erro", "A senha deve ter no maximo 72 bytes em UTF-8."));
        }

        String emailLimpo = request.email.trim().toLowerCase(Locale.ROOT);
        if (usuarioRepository.existsByEmail(emailLimpo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "Email ja cadastrado."));
        }
        if (usuarioRepository.existsByCpf(cpfLimpo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "CPF ja cadastrado."));
        }

        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(request.nomeCompleto.trim());
        usuario.setEmail(emailLimpo);
        usuario.setTelefone(telefoneLimpo);
        usuario.setCpf(cpfLimpo);
        usuario.setDataNascimento(request.dataNascimento);
        usuario.setSenha(passwordEncoder.encode(request.senha));
        usuario.setRole(UserRole.USER);
        usuario.setCriadoEm(LocalDateTime.now(ZoneOffset.UTC));

        usuarioRepository.save(usuario);
        auditService.record(usuario, "REGISTER", "SUCCESS", "USUARIO", usuario.getId(), "Usuario cadastrado.", servletRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(authResponse("Usuario cadastrado com sucesso.", usuario));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        if (!rateLimitService.allowLogin(servletRequest)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("erro", "Muitas tentativas de login. Tente novamente mais tarde."));
        }

        if (request.email == null || request.email.isBlank()
                || request.senha == null || request.senha.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Email e senha sao obrigatorios."));
        }

        String emailLimpo = request.email.trim().toLowerCase(Locale.ROOT);
        var optUsuario = usuarioRepository.findByEmail(emailLimpo);
        if (optUsuario.isEmpty()) {
            auditService.record(null, "LOGIN", "FAILURE", "USUARIO", null, "INVALID_CREDENTIALS", servletRequest);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Email ou senha incorretos."));
        }

        Usuario usuario = optUsuario.get();
        if (usuario.isBloqueado()) {
            auditService.record(usuario, "LOGIN", "BLOCKED", "USUARIO", usuario.getId(), "Conta bloqueada.", servletRequest);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("erro", "Conta bloqueada."));
        }

        if (!senhaConfere(usuario, request.senha)) {
            auditService.record(usuario, "LOGIN", "FAILURE", "USUARIO", usuario.getId(), "Senha invalida.", servletRequest);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Email ou senha incorretos."));
        }

        auditService.record(usuario, "LOGIN", "SUCCESS", "USUARIO", usuario.getId(), "Login realizado.", servletRequest);
        return ResponseEntity.ok(authResponse("Login realizado com sucesso.", usuario));
    }

    private Map<String, Object> authResponse(String mensagem, Usuario usuario) {
        return Map.of(
                "mensagem", mensagem,
                "id", usuario.getId(),
                "nome", usuario.getNomeCompleto(),
                "email", usuario.getEmail(),
                "role", usuario.getRole().name(),
                "token", jwtService.generateToken(usuario)
        );
    }

    private boolean senhaConfere(Usuario usuario, String senha) {
        if (senha.getBytes(StandardCharsets.UTF_8).length > 72) return false;
        if (usuario.getSenha() != null && usuario.getSenha().startsWith("$2")) {
            return passwordEncoder.matches(senha, usuario.getSenha());
        }

        boolean senhaLegadaConfere = usuario.getSenha() != null && usuario.getSenha().equals(hashSenha(senha));
        if (senhaLegadaConfere) {
            usuario.setSenha(passwordEncoder.encode(senha));
            usuarioRepository.save(usuario);
        }
        return senhaLegadaConfere;
    }

    private String hashSenha(String senha) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(senha.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Erro ao gerar hash da senha", e);
        }
    }

    public static class RegisterRequest {
        @NotBlank @Size(max = 255)
        public String nomeCompleto;
        @NotBlank @Email @Size(max = 254)
        public String email;
        @NotBlank @Size(max = 25)
        public String telefone;
        @NotBlank @Size(max = 20)
        public String cpf;
        @NotNull @Past
        public LocalDate dataNascimento;
        @NotBlank @Size(min = 8, max = 72)
        public String senha;
    }

    public static class LoginRequest {
        public String email;
        public String senha;
    }
}
