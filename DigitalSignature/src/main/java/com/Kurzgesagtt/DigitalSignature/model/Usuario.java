package com.Kurzgesagtt.DigitalSignature.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nomeCompleto;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String telefone;

    @Column(nullable = false, unique = true, length = 11)
    private String cpf;

    @Column(nullable = false)
    private LocalDate dataNascimento;

    @Column(nullable = false)
    private String senha;

    @Enumerated(EnumType.STRING)
    private UserRole role = UserRole.USER;

    private boolean bloqueado = false;

    private String bloqueadoMotivo;

    private Long bloqueadoPorUsuarioId;

    private LocalDateTime bloqueadoEm;

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now(ZoneOffset.UTC);

    public Usuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNomeCompleto() { return nomeCompleto; }
    public void setNomeCompleto(String nomeCompleto) { this.nomeCompleto = nomeCompleto; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public LocalDate getDataNascimento() { return dataNascimento; }
    public void setDataNascimento(LocalDate dataNascimento) { this.dataNascimento = dataNascimento; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public UserRole getRole() { return role != null ? role : UserRole.USER; }
    public void setRole(UserRole role) { this.role = role; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    public String getBloqueadoMotivo() { return bloqueadoMotivo; }
    public void setBloqueadoMotivo(String bloqueadoMotivo) { this.bloqueadoMotivo = bloqueadoMotivo; }

    public Long getBloqueadoPorUsuarioId() { return bloqueadoPorUsuarioId; }
    public void setBloqueadoPorUsuarioId(Long bloqueadoPorUsuarioId) { this.bloqueadoPorUsuarioId = bloqueadoPorUsuarioId; }

    public LocalDateTime getBloqueadoEm() { return bloqueadoEm; }
    public void setBloqueadoEm(LocalDateTime bloqueadoEm) { this.bloqueadoEm = bloqueadoEm; }

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }
}
