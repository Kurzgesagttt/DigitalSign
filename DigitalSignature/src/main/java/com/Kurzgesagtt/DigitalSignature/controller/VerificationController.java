package com.Kurzgesagtt.DigitalSignature.controller;

import com.Kurzgesagtt.DigitalSignature.model.SignatureRecord;
import com.Kurzgesagtt.DigitalSignature.repository.SignatureRecordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.format.DateTimeFormatter;
import java.util.Map;

@RestController
@RequestMapping("/v1/verify")
public class VerificationController {

    @Autowired
    private SignatureRecordRepository signatureRecordRepository;

    @GetMapping("/{code}")
    public ResponseEntity<?> verify(@PathVariable String code) {
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("valido", false, "erro", "Código inválido."));
        }

        var optRecord = signatureRecordRepository.findByVerificationCode(code);
        if (optRecord.isEmpty()) {
            return ResponseEntity.ok(Map.of("valido", false, "erro", "Assinatura não encontrada."));
        }

        SignatureRecord record = optRecord.get();
        String cpfMascarado = record.getSignatarioCpf().substring(0, 3) + ".***.***-"
                + record.getSignatarioCpf().substring(9);

        return ResponseEntity.ok(Map.of(
                "valido", true,
                "nome", record.getSignatarioNome(),
                "email", record.getSignatarioEmail(),
                "cpf", cpfMascarado,
                "assinadoEm", record.getAssinadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
        ));
    }

    @PostMapping("/{code}")
    public ResponseEntity<?> confirmIdentity(@PathVariable String code, @RequestBody ConfirmRequest request) {
        if (code == null || code.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("confirmado", false, "erro", "Código inválido."));
        }

        var optRecord = signatureRecordRepository.findByVerificationCode(code);
        if (optRecord.isEmpty()) {
            return ResponseEntity.ok(Map.of("confirmado", false, "erro", "Assinatura não encontrada."));
        }

        SignatureRecord record = optRecord.get();
        String cpfLimpo = request.cpf != null ? request.cpf.replaceAll("\\D", "") : "";
        String emailLimpo = request.email != null ? request.email.trim().toLowerCase() : "";

        boolean cpfConfere = cpfLimpo.equals(record.getSignatarioCpf());
        boolean emailConfere = emailLimpo.equals(record.getSignatarioEmail());

        if (cpfConfere && emailConfere) {
            return ResponseEntity.ok(Map.of(
                    "confirmado", true,
                    "mensagem", "Identidade do assinante confirmada com sucesso!",
                    "nome", record.getSignatarioNome(),
                    "assinadoEm", record.getAssinadoEm().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"))
            ));
        } else {
            return ResponseEntity.ok(Map.of(
                    "confirmado", false,
                    "erro", "CPF ou email não conferem com o assinante."
            ));
        }
    }

    public static class ConfirmRequest {
        public String cpf;
        public String email;
    }
}
