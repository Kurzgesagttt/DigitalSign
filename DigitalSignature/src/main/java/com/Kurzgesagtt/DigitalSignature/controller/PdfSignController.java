package com.Kurzgesagtt.DigitalSignature.controller;

import com.Kurzgesagtt.DigitalSignature.model.SignatureRecord;
import com.Kurzgesagtt.DigitalSignature.model.SignatureStatus;
import com.Kurzgesagtt.DigitalSignature.model.Usuario;
import com.Kurzgesagtt.DigitalSignature.repository.SignatureRecordRepository;
import com.Kurzgesagtt.DigitalSignature.services.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1")
public class PdfSignController {
    private final SignatureRecordRepository signatureRecordRepository;
    private final QrCodeService qrCodeService;
    private final AuthenticatedUserService authenticatedUserService;
    private final HashService hashService;
    private final AuditService auditService;
    private final RequestMetadataService requestMetadataService;

    @Value("${app.base-url}")
    private String baseUrl;

    public PdfSignController(
            SignatureRecordRepository signatureRecordRepository,
            QrCodeService qrCodeService,
            AuthenticatedUserService authenticatedUserService,
            HashService hashService,
            AuditService auditService,
            RequestMetadataService requestMetadataService
    ) {
        this.signatureRecordRepository = signatureRecordRepository;
        this.qrCodeService = qrCodeService;
        this.authenticatedUserService = authenticatedUserService;
        this.hashService = hashService;
        this.auditService = auditService;
        this.requestMetadataService = requestMetadataService;
    }

    @GetMapping("/sign")
    public String index() {
        return "Use o metodo POST autenticado para enviar o PDF a ser assinado.";
    }

    @PostMapping("/sign")
    public ResponseEntity<?> signPdf(@RequestParam("file") MultipartFile file, HttpServletRequest request) {
        Usuario usuario = authenticatedUserService.getCurrentUser();
        try {
            if (file == null || file.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Arquivo PDF e obrigatorio."));
            }
            if (!MediaType.APPLICATION_PDF_VALUE.equalsIgnoreCase(file.getContentType())) {
                return ResponseEntity.badRequest().body(Map.of("erro", "Envie um arquivo PDF valido."));
            }

            byte[] originalPdf = file.getBytes();
            String verificationCode = generateVerificationCode();
            String verificationUrl = baseUrl + "/verificar/" + verificationCode;
            byte[] qrCodeBytes = qrCodeService.generateQrCode(verificationUrl, 200, 200);

            KeyPair keyPair = KeyGenerator.generateKeyPair();
            byte[] signedPdf = PdfMemorySignService.signPdf(
                    originalPdf,
                    keyPair.getPrivate(),
                    usuario.getNomeCompleto(),
                    usuario.getEmail(),
                    usuario.getCpf(),
                    verificationCode,
                    verificationUrl,
                    qrCodeBytes
            );

            SignatureRecord record = new SignatureRecord();
            record.setVerificationCode(verificationCode);
            record.setUsuario(usuario);
            record.setSignatarioNome(usuario.getNomeCompleto());
            record.setSignatarioEmail(usuario.getEmail());
            record.setSignatarioCpf(usuario.getCpf());
            record.setAssinadoEm(LocalDateTime.now());
            record.setStatus(SignatureStatus.VALIDA);
            record.setIpAddress(requestMetadataService.getClientIp(request));
            record.setUserAgent(requestMetadataService.getUserAgent(request));
            record.setOriginalPdfHash(hashService.sha256(originalPdf));
            record.setSignedPdfHash(hashService.sha256(signedPdf));
            signatureRecordRepository.save(record);

            auditService.record(usuario, "SIGNATURE_CREATED", "SUCCESS", "SIGNATURE", record.getId(), "PDF assinado.", request);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "documento_assinado.pdf");
            headers.set("X-Verification-Code", verificationCode);

            return new ResponseEntity<>(signedPdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            auditService.record(usuario, "SIGNATURE_CREATED", "FAILURE", "SIGNATURE", null, e.getMessage(), request);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao assinar PDF."));
        }
    }

    private String generateVerificationCode() {
        String code;
        do {
            code = UUID.randomUUID().toString().substring(0, 8);
        } while (signatureRecordRepository.findByVerificationCode(code).isPresent());
        return code;
    }
}
