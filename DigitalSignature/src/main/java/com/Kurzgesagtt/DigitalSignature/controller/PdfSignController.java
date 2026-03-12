package com.Kurzgesagtt.DigitalSignature.controller;

import com.Kurzgesagtt.DigitalSignature.model.SignatureRecord;
import com.Kurzgesagtt.DigitalSignature.repository.SignatureRecordRepository;
import com.Kurzgesagtt.DigitalSignature.services.KeyGenerator;
import com.Kurzgesagtt.DigitalSignature.services.PdfMemorySignService;
import com.Kurzgesagtt.DigitalSignature.services.QrCodeService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    PdfMemorySignService signService;

    @Autowired
    private SignatureRecordRepository signatureRecordRepository;

    @Autowired
    private QrCodeService qrCodeService;

    @Value("${app.base-url}")
    private String baseUrl;

    @GetMapping("/sign")
    public String index() {
        return "Use o método POST para enviar o PDF a ser assinado.";
    }

    @PostMapping("/sign")
    public ResponseEntity<?> signPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("nome") String nome,
            @RequestParam("email") String email,
            @RequestParam("cpf") String cpf
    ) {
        try {
            String cpfLimpo = cpf.replaceAll("\\D", "");
            if (cpfLimpo.length() != 11) {
                return ResponseEntity.badRequest().body(Map.of("erro", "CPF inválido."));
            }

            // Gera código de verificação único
            String verificationCode = UUID.randomUUID().toString().substring(0, 8);
            String verificationUrl = baseUrl + "/verificar/" + verificationCode;

            // Gera QR Code
            byte[] qrCodeBytes = qrCodeService.generateQrCode(verificationUrl, 200, 200);

            // Assina o PDF
            KeyPair keyPair = KeyGenerator.generateKeyPair();
            byte[] signedPdf = PdfMemorySignService.signPdf(
                    file.getBytes(),
                    keyPair.getPrivate(),
                    nome.trim(),
                    email.trim().toLowerCase(),
                    cpfLimpo,
                    verificationCode,
                    verificationUrl,
                    qrCodeBytes
            );

            // Salva registro da assinatura
            SignatureRecord record = new SignatureRecord();
            record.setVerificationCode(verificationCode);
            record.setSignatarioNome(nome.trim());
            record.setSignatarioEmail(email.trim().toLowerCase());
            record.setSignatarioCpf(cpfLimpo);
            record.setAssinadoEm(LocalDateTime.now());
            signatureRecordRepository.save(record);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "documento_assinado.pdf");
            headers.set("X-Verification-Code", verificationCode);

            return new ResponseEntity<>(signedPdf, headers, HttpStatus.OK);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("erro", "Erro ao assinar PDF: " + e.getMessage()));
        }
    }
}
