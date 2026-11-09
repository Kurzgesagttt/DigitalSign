package com.Kurzgesagtt.DigitalSignature.repository;

import com.Kurzgesagtt.DigitalSignature.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findAllByOrderByOccurredAtDesc(Pageable pageable);
}
