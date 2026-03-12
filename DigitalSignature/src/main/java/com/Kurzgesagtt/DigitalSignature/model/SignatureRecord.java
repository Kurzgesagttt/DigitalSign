package com.Kurzgesagtt.DigitalSignature.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "signature_records")
public class SignatureRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String verificationCode;

    @Column(nullable = false)
    private String signatarioNome;

    @Column(nullable = false)
    private String signatarioEmail;

    @Column(nullable = false, length = 11)
    private String signatarioCpf;

    @Column(nullable = false)
    private LocalDateTime assinadoEm = LocalDateTime.now();

    public SignatureRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public String getSignatarioNome() { return signatarioNome; }
    public void setSignatarioNome(String signatarioNome) { this.signatarioNome = signatarioNome; }

    public String getSignatarioEmail() { return signatarioEmail; }
    public void setSignatarioEmail(String signatarioEmail) { this.signatarioEmail = signatarioEmail; }

    public String getSignatarioCpf() { return signatarioCpf; }
    public void setSignatarioCpf(String signatarioCpf) { this.signatarioCpf = signatarioCpf; }

    public LocalDateTime getAssinadoEm() { return assinadoEm; }
    public void setAssinadoEm(LocalDateTime assinadoEm) { this.assinadoEm = assinadoEm; }
}
