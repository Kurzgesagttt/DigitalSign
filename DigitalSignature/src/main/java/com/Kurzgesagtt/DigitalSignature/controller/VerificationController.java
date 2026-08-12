package com.Kurzgesagtt.DigitalSignature.controller;

import com.Kurzgesagtt.DigitalSignature.model.SignatureRecord;
import com.Kurzgesagtt.DigitalSignature.model.SignatureStatus;
import com.Kurzgesagtt.DigitalSignature.repository.SignatureRecordRepository;
import com.Kurzgesagtt.DigitalSignature.services.AuditService;
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

    public VerificationController(SignatureRecordRepository signatureRecordRepository, AuditService auditService) {
        this.signatureRecordRepository = signatureRecordRepository;
        this.auditService = auditService;
    }

    @GetMapping("/{code}")
    public ResponseEntity<?> verify(@PathVariable String code, HttpServletRequest request) {
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("valido", false, "erro", "Codigo invalido."));
        }

        var optRecord = signatureRecordRepository.findByVerificationCode(code);
        if (optRecord.isEmpty()) {
            auditService.record(null, "SIGNATURE_VERIFY", "NOT_FOUND", "SIGNATURE", null, "Codigo: " + code, request);
            return ResponseEntity.ok(Map.of("valido", false, "erro", "Assinatura nao encontrada."));
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

    @PostMapping("/{code}")
    public ResponseEntity<?> confirmIdentity(
            @PathVariable String code,
            @RequestBody ConfirmRequest requestBody,
            HttpServletRequest request
    ) {
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("confirmado", false, "erro", "Codigo invalido."));
        }

        var optRecord = signatureRecordRepository.findByVerificationCode(code);
        if (optRecord.isEmpty()) {
            auditService.record(null, "SIGNATURE_IDENTITY_CONFIRM", "NOT_FOUND", "SIGNATURE", null, "Codigo: " + code, request);
            return ResponseEntity.ok(Map.of("confirmado", false, "erro", "Assinatura nao encontrada."));
        }

        SignatureRecord record = optRecord.get();
        if (record.getStatus() != SignatureStatus.VALIDA) {
            auditService.record(null, "SIGNATURE_IDENTITY_CONFIRM", "INVALID_STATUS", "SIGNATURE", record.getId(), record.getStatus().name(), request);
            return ResponseEntity.ok(Map.of(
                    "confirmado", false,
                    "status", record.getStatus().name(),
                    "erro", "Assinatura nao esta valida."
            ));
        }

        String cpfLimpo = requestBody.cpf != null ? requestBody.cpf.replaceAll("\\D", "") : "";
        String emailLimpo = requestBody.email != null ? requestBody.email.trim().toLowerCase() : "";

        boolean cpfConfere = cpfLimpo.equals(record.getSignatarioCpf());
        boolean emailConfere = emailLimpo.equals(record.getSignatarioEmail());

        if (cpfConfere && emailConfere) {
            auditService.record(null, "SIGNATURE_IDENTITY_CONFIRM", "SUCCESS", "SIGNATURE", record.getId(), "Identidade confirmada.", request);
            return ResponseEntity.ok(Map.of(
                    "confirmado", true,
                    "mensagem", "Identidade do assinante confirmada com sucesso.",
                    "nome", record.getSignatarioNome(),
                    "assinadoEm", formatDate(record)
            ));
        }

        auditService.record(null, "SIGNATURE_IDENTITY_CONFIRM", "FAILURE", "SIGNATURE", record.getId(), "CPF ou email divergente.", request);
        return ResponseEntity.ok(Map.of(
                "confirmado", false,
                "erro", "CPF ou email nao conferem com o assinante."
        ));
    }

    private Map<String, Object> baseVerifyResponse(SignatureRecord record) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("valido", record.getStatus() == SignatureStatus.VALIDA);
        response.put("status", record.getStatus().name());
        response.put("nome", record.getSignatarioNome());
        response.put("email", record.getSignatarioEmail());
        response.put("cpf", maskCpf(record.getSignatarioCpf()));
        response.put("assinadoEm", formatDate(record));
        return response;
    }

    private String formatDate(SignatureRecord record) {
        return record.getAssinadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return "***";
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }

    public static class ConfirmRequest {
        public String cpf;
        public String email;
    }
}
