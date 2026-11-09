package com.Kurzgesagtt.DigitalSignature.repository;

import com.Kurzgesagtt.DigitalSignature.model.SignatureRecord;
import com.Kurzgesagtt.DigitalSignature.model.SignatureStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface SignatureRecordRepository extends JpaRepository<SignatureRecord, Long>, JpaSpecificationExecutor<SignatureRecord> {
    Optional<SignatureRecord> findByVerificationCode(String verificationCode);
    long countByStatus(SignatureStatus status);

    @Query("select s.signatarioEmail, s.signatarioNome, count(s) from SignatureRecord s group by s.signatarioEmail, s.signatarioNome order by count(s) desc")
    Page<Object[]> countSignaturesBySigner(Pageable pageable);
}
