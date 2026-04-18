package com.Kurzgesagtt.DigitalSignature.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

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

    @Column(nullable = false)
    private LocalDateTime criadoEm = LocalDateTime.now();

    /** LGPD Art. 7 — Registro explícito do consentimento do titular */
    @Column(nullable = false)
    private boolean consentimentoLGPD = false;

    @Column
    private LocalDateTime consentimentoEm;

    /** LGPD Art. 18 inciso VI — Direito ao esquecimento (soft delete) */
    @Column
    private LocalDateTime deletadoEm;

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

    public LocalDateTime getCriadoEm() { return criadoEm; }
    public void setCriadoEm(LocalDateTime criadoEm) { this.criadoEm = criadoEm; }

    public boolean isConsentimentoLGPD() { return consentimentoLGPD; }
    public void setConsentimentoLGPD(boolean consentimentoLGPD) { this.consentimentoLGPD = consentimentoLGPD; }

    public LocalDateTime getConsentimentoEm() { return consentimentoEm; }
    public void setConsentimentoEm(LocalDateTime consentimentoEm) { this.consentimentoEm = consentimentoEm; }

    public LocalDateTime getDeletadoEm() { return deletadoEm; }
    public void setDeletadoEm(LocalDateTime deletadoEm) { this.deletadoEm = deletadoEm; }

    public boolean isAtivo() { return deletadoEm == null; }
}
