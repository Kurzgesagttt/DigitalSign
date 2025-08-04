package com.Kurzgesagtt.DigitalSignature.services;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.util.Calendar;

//Este serviço assina utilizando criptografia assimétrica na memória, ou seja,
// gerando uma chave publica e privada q fica na memoria local. Por tanto, assinatura invalida
@Service
public class PdfMemorySignService {

    // Serviço principal chamado pelo controller
    public static byte[] signPdf(byte[] pdfBytes, PrivateKey privateKey) throws IOException {
        try (PDDocument document = PDDocument.load(pdfBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED); // Compatível com Adobe
            signature.setName("API Kurzgesagtt");
            signature.setLocation("Server localhost:8080");
            signature.setSignDate(Calendar.getInstance());
            //gerencia o documento aqui
            document.addSignature(signature, new PdfBoxSignature(privateKey));
            document.saveIncremental(outputStream);

            return outputStream.toByteArray();
        }
    }

    // Classe interna que implementa a lógica de assinatura com o PDFBox, e o provider é o Bouncy Castle
    private static class PdfBoxSignature implements SignatureInterface {
        private final PrivateKey privateKey;

        public PdfBoxSignature(PrivateKey privateKey) {
            this.privateKey = privateKey;
        }

        @Override
        public byte[] sign(InputStream content) throws IOException {
            try {
                byte[] contentBytes = IOUtils.toByteArray(content);

                MessageDigest digest = MessageDigest.getInstance("SHA256");
                byte[] hash = digest.digest(contentBytes);

                Signature signature = Signature.getInstance("SHA256withRSA");
                signature.initSign(privateKey);
                signature.update(hash);

                return signature.sign();
            } catch (Exception e) {
                throw new IOException("Erro ao assinar o conteúdo do PDF", e);
            }
        }
    }
}
