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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false)
    private String signatarioNome;

    @Column(nullable = false)
    private String signatarioEmail;

    @Column(nullable = false, length = 11)
    private String signatarioCpf;

    @Column(nullable = false)
    private LocalDateTime assinadoEm = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    private SignatureStatus status = SignatureStatus.VALIDA;

    private String ipAddress;

    @Column(length = 512)
    private String userAgent;

    @Column(length = 64)
    private String originalPdfHash;

    @Column(length = 64)
    private String signedPdfHash;

    private Long revogadoPorUsuarioId;

    private LocalDateTime revogadoEm;

    @Column(length = 1000)
    private String revogacaoMotivo;

    public SignatureRecord() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getVerificationCode() { return verificationCode; }
    public void setVerificationCode(String verificationCode) { this.verificationCode = verificationCode; }

    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public String getSignatarioNome() { return signatarioNome; }
    public void setSignatarioNome(String signatarioNome) { this.signatarioNome = signatarioNome; }

    public String getSignatarioEmail() { return signatarioEmail; }
    public void setSignatarioEmail(String signatarioEmail) { this.signatarioEmail = signatarioEmail; }

    public String getSignatarioCpf() { return signatarioCpf; }
    public void setSignatarioCpf(String signatarioCpf) { this.signatarioCpf = signatarioCpf; }

    public LocalDateTime getAssinadoEm() { return assinadoEm; }
    public void setAssinadoEm(LocalDateTime assinadoEm) { this.assinadoEm = assinadoEm; }

    public SignatureStatus getStatus() { return status != null ? status : SignatureStatus.VALIDA; }
    public void setStatus(SignatureStatus status) { this.status = status; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }

    public String getOriginalPdfHash() { return originalPdfHash; }
    public void setOriginalPdfHash(String originalPdfHash) { this.originalPdfHash = originalPdfHash; }

    public String getSignedPdfHash() { return signedPdfHash; }
    public void setSignedPdfHash(String signedPdfHash) { this.signedPdfHash = signedPdfHash; }

    public Long getRevogadoPorUsuarioId() { return revogadoPorUsuarioId; }
    public void setRevogadoPorUsuarioId(Long revogadoPorUsuarioId) { this.revogadoPorUsuarioId = revogadoPorUsuarioId; }

    public LocalDateTime getRevogadoEm() { return revogadoEm; }
    public void setRevogadoEm(LocalDateTime revogadoEm) { this.revogadoEm = revogadoEm; }

    public String getRevogacaoMotivo() { return revogacaoMotivo; }
    public void setRevogacaoMotivo(String revogacaoMotivo) { this.revogacaoMotivo = revogacaoMotivo; }
}
