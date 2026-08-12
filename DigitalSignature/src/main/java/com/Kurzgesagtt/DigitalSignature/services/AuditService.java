package com.Kurzgesagtt.DigitalSignature.services;

import com.Kurzgesagtt.DigitalSignature.model.AuditLog;
import com.Kurzgesagtt.DigitalSignature.model.Usuario;
import com.Kurzgesagtt.DigitalSignature.repository.AuditLogRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final RequestMetadataService requestMetadataService;

    public AuditService(AuditLogRepository auditLogRepository, RequestMetadataService requestMetadataService) {
        this.auditLogRepository = auditLogRepository;
        this.requestMetadataService = requestMetadataService;
    }

    public void record(
            Usuario actor,
            String eventType,
            String result,
            String targetType,
            Long targetId,
            String details,
            HttpServletRequest request
    ) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        if (actor != null) {
            log.setActorEmail(actor.getEmail());
            log.setActorRole(actor.getRole());
        }
        log.setEventType(eventType);
        log.setResult(result);
        log.setTargetType(targetType);
        log.setTargetId(targetId);
        log.setDetails(truncate(details));
        if (request != null) {
            log.setIpAddress(requestMetadataService.getClientIp(request));
            log.setUserAgent(requestMetadataService.getUserAgent(request));
        }
        auditLogRepository.save(log);
    }

    private String truncate(String value) {
        if (value == null) {
            return null;
        }
        return value.length() > 1000 ? value.substring(0, 1000) : value;
    }
}
