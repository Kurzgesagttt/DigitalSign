package com.Kurzgesagtt.DigitalSignature.controller;

import com.Kurzgesagtt.DigitalSignature.model.*;
import com.Kurzgesagtt.DigitalSignature.repository.AuditLogRepository;
import com.Kurzgesagtt.DigitalSignature.repository.SignatureRecordRepository;
import com.Kurzgesagtt.DigitalSignature.repository.UsuarioRepository;
import com.Kurzgesagtt.DigitalSignature.services.AuditService;
import com.Kurzgesagtt.DigitalSignature.services.AuthenticatedUserService;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/v1/admin")
public class AdminController {
    private final UsuarioRepository usuarioRepository;
    private final SignatureRecordRepository signatureRecordRepository;
    private final AuditLogRepository auditLogRepository;
    private final AuthenticatedUserService authenticatedUserService;
    private final AuditService auditService;

    public AdminController(
            UsuarioRepository usuarioRepository,
            SignatureRecordRepository signatureRecordRepository,
            AuditLogRepository auditLogRepository,
            AuthenticatedUserService authenticatedUserService,
            AuditService auditService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.signatureRecordRepository = signatureRecordRepository;
        this.auditLogRepository = auditLogRepository;
        this.authenticatedUserService = authenticatedUserService;
        this.auditService = auditService;
    }

    @GetMapping("/users")
    public ResponseEntity<?> listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        var users = usuarioRepository.findAll(PageRequest.of(page, clampSize(size), Sort.by(Sort.Direction.DESC, "criadoEm")))
                .map(this::userResponse);
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/users/{id}/block")
    public ResponseEntity<?> blockUser(
            @PathVariable Long id,
            @RequestBody BlockUserRequest request,
            HttpServletRequest servletRequest
    ) {
        Usuario admin = authenticatedUserService.getCurrentUser();
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario nao encontrado."));

        if (usuario.getId().equals(admin.getId())) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Admin nao pode bloquear a propria conta."));
        }
        if (request.motivo == null || request.motivo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Motivo e obrigatorio."));
        }

        usuario.setBloqueado(true);
        usuario.setBloqueadoMotivo(request.motivo.trim());
        usuario.setBloqueadoPorUsuarioId(admin.getId());
        usuario.setBloqueadoEm(LocalDateTime.now(ZoneOffset.UTC));
        usuarioRepository.save(usuario);

        auditService.record(admin, "USER_BLOCKED", "SUCCESS", "USUARIO", usuario.getId(), "ACCOUNT_BLOCKED", servletRequest);
        return ResponseEntity.ok(userResponse(usuario));
    }

    @PatchMapping("/users/{id}/unblock")
    public ResponseEntity<?> unblockUser(@PathVariable Long id, HttpServletRequest servletRequest) {
        Usuario admin = authenticatedUserService.getCurrentUser();
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Usuario nao encontrado."));

        usuario.setBloqueado(false);
        usuario.setBloqueadoMotivo(null);
        usuario.setBloqueadoPorUsuarioId(null);
        usuario.setBloqueadoEm(null);
        usuarioRepository.save(usuario);

        auditService.record(admin, "USER_UNBLOCKED", "SUCCESS", "USUARIO", usuario.getId(), "Usuario desbloqueado.", servletRequest);
        return ResponseEntity.ok(userResponse(usuario));
    }

    @GetMapping("/signatures")
    public ResponseEntity<?> listSignatures(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) SignatureStatus status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        Specification<SignatureRecord> spec = signatureSpec(code, usuarioId, status, from, to);
        var signatures = signatureRecordRepository.findAll(
                spec,
                PageRequest.of(page, clampSize(size), Sort.by(Sort.Direction.DESC, "assinadoEm"))
        ).map(this::signatureResponse);
        return ResponseEntity.ok(signatures);
    }

    @PatchMapping("/signatures/{id}/revoke")
    public ResponseEntity<?> revokeSignature(
            @PathVariable Long id,
            @RequestBody RevokeSignatureRequest request,
            HttpServletRequest servletRequest
    ) {
        Usuario admin = authenticatedUserService.getCurrentUser();
        SignatureRecord signature = signatureRecordRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Assinatura nao encontrada."));
        if (request.motivo == null || request.motivo.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Motivo e obrigatorio."));
        }

        signature.setStatus(SignatureStatus.REVOGADA);
        signature.setRevogacaoMotivo(request.motivo.trim());
        signature.setRevogadoPorUsuarioId(admin.getId());
        signature.setRevogadoEm(LocalDateTime.now(ZoneOffset.UTC));
        signatureRecordRepository.save(signature);

        auditService.record(admin, "SIGNATURE_REVOKED", "SUCCESS", "SIGNATURE", signature.getId(), "SIGNATURE_REVOKED", servletRequest);
        return ResponseEntity.ok(signatureResponse(signature));
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> metrics() {
        List<Map<String, Object>> topSigners = signatureRecordRepository.countSignaturesBySigner(PageRequest.of(0, 10))
                .map(row -> Map.of(
                        "email", row[0],
                        "nome", row[1],
                        "total", row[2]
                ))
                .getContent();

        return ResponseEntity.ok(Map.of(
                "totalUsuarios", usuarioRepository.count(),
                "totalAssinaturas", signatureRecordRepository.count(),
                "assinaturasValidas", signatureRecordRepository.countByStatus(SignatureStatus.VALIDA),
                "assinaturasRevogadas", signatureRecordRepository.countByStatus(SignatureStatus.REVOGADA),
                "assinaturasSuspeitas", signatureRecordRepository.countByStatus(SignatureStatus.SUSPEITA),
                "topAssinantes", topSigners
        ));
    }

    @GetMapping("/audit-logs")
    public ResponseEntity<?> auditLogs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        var logs = auditLogRepository.findAllByOrderByOccurredAtDesc(PageRequest.of(page, clampSize(size)))
                .map(this::auditLogResponse);
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/reports/signatures.csv")
    public ResponseEntity<String> exportSignaturesCsv(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) SignatureStatus status,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            HttpServletRequest servletRequest
    ) {
        Usuario admin = authenticatedUserService.getCurrentUser();
        Specification<SignatureRecord> spec = signatureSpec(code, usuarioId, status, from, to);
        List<SignatureRecord> records = signatureRecordRepository.findAll(spec, Sort.by(Sort.Direction.DESC, "assinadoEm"));

        StringBuilder csv = new StringBuilder();
        csv.append("codigo,nome,email,cpf_mascarado,status,assinado_em,ip,hash_original,hash_assinado\n");
        for (SignatureRecord record : records) {
            csv.append(csvLine(List.of(
                    record.getVerificationCode(),
                    record.getSignatarioNome(),
                    record.getSignatarioEmail(),
                    maskCpf(record.getSignatarioCpf()),
                    record.getStatus().name(),
                    formatDate(record.getAssinadoEm()),
                    nullToEmpty(record.getIpAddress()),
                    nullToEmpty(record.getOriginalPdfHash()),
                    nullToEmpty(record.getSignedPdfHash())
            )));
        }

        auditService.record(admin, "SIGNATURE_REPORT_EXPORTED", "SUCCESS", "REPORT", null, "Exportacao CSV de assinaturas.", servletRequest);
        return csvResponse("assinaturas.csv", csv.toString());
    }

    @GetMapping("/reports/audit-logs.csv")
    public ResponseEntity<String> exportAuditLogsCsv(HttpServletRequest servletRequest) {
        Usuario admin = authenticatedUserService.getCurrentUser();
        var logs = auditLogRepository.findAll(PageRequest.of(0, 5000, Sort.by(Sort.Direction.DESC, "occurredAt"))).getContent();

        StringBuilder csv = new StringBuilder();
        csv.append("data,event_type,result,actor_email,actor_role,target_type,target_id,ip,details\n");
        for (AuditLog log : logs) {
            csv.append(csvLine(List.of(
                    formatDate(log.getOccurredAt()),
                    log.getEventType(),
                    log.getResult(),
                    nullToEmpty(log.getActorEmail()),
                    log.getActorRole() != null ? log.getActorRole().name() : "",
                    nullToEmpty(log.getTargetType()),
                    log.getTargetId() != null ? String.valueOf(log.getTargetId()) : "",
                    nullToEmpty(log.getIpAddress()),
                    nullToEmpty(log.getDetails())
            )));
        }

        auditService.record(admin, "AUDIT_REPORT_EXPORTED", "SUCCESS", "REPORT", null, "Exportacao CSV de auditoria.", servletRequest);
        return csvResponse("auditoria.csv", csv.toString());
    }

    private Specification<SignatureRecord> signatureSpec(
            String code,
            Long usuarioId,
            SignatureStatus status,
            String from,
            String to
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (code != null && !code.isBlank()) {
                predicates.add(cb.equal(root.get("verificationCode"), code.trim()));
            }
            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("usuario").get("id"), usuarioId));
            }
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (from != null && !from.isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("assinadoEm"), LocalDate.parse(from).atStartOfDay()));
            }
            if (to != null && !to.isBlank()) {
                predicates.add(cb.lessThan(root.get("assinadoEm"), LocalDate.parse(to).plusDays(1).atStartOfDay()));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private Map<String, Object> userResponse(Usuario usuario) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", usuario.getId());
        response.put("nome", usuario.getNomeCompleto());
        response.put("email", usuario.getEmail());
        response.put("cpf", maskCpf(usuario.getCpf()));
        response.put("role", usuario.getRole().name());
        response.put("bloqueado", usuario.isBloqueado());
        response.put("bloqueadoMotivo", usuario.getBloqueadoMotivo());
        response.put("criadoEm", formatDate(usuario.getCriadoEm()));
        return response;
    }

    private Map<String, Object> signatureResponse(SignatureRecord record) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", record.getId());
        response.put("verificationCode", record.getVerificationCode());
        response.put("usuarioId", record.getUsuario() != null ? record.getUsuario().getId() : null);
        response.put("nome", record.getSignatarioNome());
        response.put("email", record.getSignatarioEmail());
        response.put("cpf", maskCpf(record.getSignatarioCpf()));
        response.put("status", record.getStatus().name());
        response.put("assinadoEm", formatDate(record.getAssinadoEm()));
        response.put("ipAddress", record.getIpAddress());
        response.put("userAgent", record.getUserAgent());
        response.put("originalPdfHash", record.getOriginalPdfHash());
        response.put("signedPdfHash", record.getSignedPdfHash());
        response.put("revogadoEm", formatDate(record.getRevogadoEm()));
        response.put("revogacaoMotivo", record.getRevogacaoMotivo());
        return response;
    }

    private Map<String, Object> auditLogResponse(AuditLog log) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("id", log.getId());
        response.put("eventType", log.getEventType());
        response.put("result", log.getResult());
        response.put("actorEmail", log.getActorEmail());
        response.put("actorRole", log.getActorRole() != null ? log.getActorRole().name() : null);
        response.put("targetType", log.getTargetType());
        response.put("targetId", log.getTargetId());
        response.put("ipAddress", log.getIpAddress());
        response.put("userAgent", log.getUserAgent());
        response.put("details", log.getDetails());
        response.put("occurredAt", formatDate(log.getOccurredAt()));
        return response;
    }

    private int clampSize(int size) {
        return Math.max(1, Math.min(size, 100));
    }

    private String maskCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return "***";
        }
        return cpf.substring(0, 3) + ".***.***-" + cpf.substring(9);
    }

    private String formatDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
    }

    private ResponseEntity<String> csvResponse(String filename, String csv) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csv);
    }

    private String csvLine(List<String> values) {
        return values.stream()
                .map(this::csvEscape)
                .reduce((left, right) -> left + "," + right)
                .orElse("") + "\n";
    }

    private String csvEscape(String value) {
        String safeValue = nullToEmpty(value);
        if (!safeValue.isEmpty() && "=+-@".indexOf(safeValue.charAt(0)) >= 0) {
            safeValue = "'" + safeValue;
        }
        if (safeValue.contains(",") || safeValue.contains("\"") || safeValue.contains("\n") || safeValue.contains("\r")) {
            return "\"" + safeValue.replace("\"", "\"\"") + "\"";
        }
        return safeValue;
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    public static class BlockUserRequest {
        public String motivo;
    }

    public static class RevokeSignatureRequest {
        public String motivo;
    }
}
