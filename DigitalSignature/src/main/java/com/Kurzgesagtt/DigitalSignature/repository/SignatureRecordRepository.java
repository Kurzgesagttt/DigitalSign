package com.Kurzgesagtt.DigitalSignature.repository;

import com.Kurzgesagtt.DigitalSignature.model.SignatureRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SignatureRecordRepository extends JpaRepository<SignatureRecord, Long> {
    Optional<SignatureRecord> findByVerificationCode(String verificationCode);
}
