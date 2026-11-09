package com.Kurzgesagtt.DigitalSignature.controller;

import com.Kurzgesagtt.DigitalSignature.model.SignatureRecord;
import com.Kurzgesagtt.DigitalSignature.model.SignatureStatus;
import com.Kurzgesagtt.DigitalSignature.repository.SignatureRecordRepository;
import com.Kurzgesagtt.DigitalSignature.services.AuditService;
import com.Kurzgesagtt.DigitalSignature.services.RateLimitService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/v1/verify")
public class VerificationController {
    private final SignatureRecordRepository signatureRecordRepository;
    private final AuditService auditService;
    private final RateLimitService rateLimitService;

    public VerificationController(SignatureRecordRepository signatureRecordRepository, AuditService auditService, RateLimitService rateLimitService) {
        this.signatureRecordRepository = signatureRecordRepository;
        this.auditService = auditService;
        this.rateLimitService = rateLimitService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> verify(@PathVariable String code, HttpServletRequest request) {
        if (!rateLimitService.allowVerification(request)) {
            return ResponseEntity.status(429).body(Map.of("valido", false, "erro", "Muitas tentativas. Tente novamente mais tarde."));
        }
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("valido", false, "erro", "Codigo invalido."));
        }

        var optRecord = signatureRecordRepository.findByVerificationCode(code);
        if (optRecord.isEmpty()) {
            auditService.record(null, "SIGNATURE_VERIFY", "NOT_FOUND", "SIGNATURE", null, "CODE_NOT_FOUND", request);
            return ResponseEntity.ok(Map.of("valido", false, "erro", "Codigo nao localizado ou assinatura invalida."));
        }

        SignatureRecord record = optRecord.get();
        auditService.record(null, "SIGNATURE_VERIFY", "SUCCESS", "SIGNATURE", record.getId(), "Consulta publica.", request);

        Map<String, Object> response = baseVerifyResponse(record);
        if (record.getStatus() != SignatureStatus.VALIDA) {
            response.put("valido", false);
            response.put("erro", "Assinatura " + record.getStatus().name().toLowerCase() + ".");
        }
        return ResponseEntity.ok(response);
    }

    private Map<String, Object> baseVerifyResponse(SignatureRecord record) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("valido", record.getStatus() == SignatureStatus.VALIDA);
        response.put("status", record.getStatus().name());
        response.put("assinadoEm", formatDate(record));
        return response;
    }

    private String formatDate(SignatureRecord record) {
        return record.getAssinadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss 'UTC'"));
    }
}
