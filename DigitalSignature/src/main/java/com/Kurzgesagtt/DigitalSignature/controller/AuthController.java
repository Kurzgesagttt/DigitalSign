package com.Kurzgesagtt.DigitalSignature.controller;

import com.Kurzgesagtt.DigitalSignature.model.Usuario;
import com.Kurzgesagtt.DigitalSignature.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HexFormat;
import java.util.Map;

@RestController
@RequestMapping("/v1/auth")
public class AuthController {

    @Autowired
    private UsuarioRepository usuarioRepository;

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

        String cpfLimpo = request.cpf.replaceAll("\\D", "");
        if (cpfLimpo.length() != 11) {
            return ResponseEntity.badRequest().body(Map.of("erro", "CPF inválido."));
        }

        if (usuarioRepository.existsByEmail(request.email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "Email já cadastrado."));
        }
        if (usuarioRepository.existsByCpf(cpfLimpo)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("erro", "CPF já cadastrado."));
        }

        Usuario usuario = new Usuario();
        usuario.setNomeCompleto(request.nomeCompleto.trim());
        usuario.setEmail(request.email.trim().toLowerCase());
        usuario.setTelefone(request.telefone.replaceAll("\\D", ""));
        usuario.setCpf(cpfLimpo);
        usuario.setDataNascimento(request.dataNascimento);
        usuario.setSenha(hashSenha(request.senha));
        usuario.setCriadoEm(LocalDateTime.now());

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

        var optUsuario = usuarioRepository.findByEmail(request.email.trim().toLowerCase());
        if (optUsuario.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Email ou senha incorretos."));
        }

        Usuario usuario = optUsuario.get();
        if (!usuario.getSenha().equals(hashSenha(request.senha))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Email ou senha incorretos."));
        }

        return ResponseEntity.ok(Map.of(
                "mensagem", "Login realizado com sucesso.",
                "id", usuario.getId(),
                "nome", usuario.getNomeCompleto(),
                "email", usuario.getEmail(),
                "cpf", usuario.getCpf()
        ));
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
        public String nomeCompleto;
        public String email;
        public String telefone;
        public String cpf;
        public LocalDate dataNascimento;
        public String senha;
    }

    public static class LoginRequest {
        public String email;
        public String senha;
    }
}
