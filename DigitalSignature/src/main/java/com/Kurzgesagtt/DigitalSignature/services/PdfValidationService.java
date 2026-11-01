package com.Kurzgesagtt.DigitalSignature.services;

import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

@Service
public class PdfValidationService {

    public void validate(byte[] content) {
        if (content == null || content.length < 5 || !new String(content, 0, 5, StandardCharsets.US_ASCII).equals("%PDF-")) {
            throw new IllegalArgumentException("Arquivo nao possui cabecalho PDF valido.");
        }

        try (PDDocument document = PDDocument.load(new ByteArrayInputStream(content), MemoryUsageSetting.setupMainMemoryOnly())) {
            if (document.isEncrypted()) {
                throw new IllegalArgumentException("PDF protegido por senha nao e aceito.");
            }
            if (document.getNumberOfPages() == 0) {
                throw new IllegalArgumentException("PDF sem paginas nao e aceito.");
            }
        } catch (IOException exception) {
            throw new IllegalArgumentException("PDF corrompido ou nao suportado.", exception);
        }
    }
}
