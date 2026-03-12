package com.Kurzgesagtt.DigitalSignature.services;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

@Service
public class PdfMemorySignService {

    public static byte[] signPdf(byte[] pdfBytes, PrivateKey privateKey, String nome, String email, String cpf,
                                  String verificationCode, String verificationUrl, byte[] qrCodeBytes) throws IOException {
        byte[] modifiedPdfBytes;
        try (PDDocument document = PDDocument.load(pdfBytes);
             ByteArrayOutputStream tempStream = new ByteArrayOutputStream()) {

            adicionarMetadados(document, nome, email, cpf);
            adicionarPaginaDeAssinatura(document, nome, email, cpf, verificationCode, verificationUrl, qrCodeBytes);

            document.save(tempStream);
            modifiedPdfBytes = tempStream.toByteArray();
        }

        try (PDDocument document = PDDocument.load(modifiedPdfBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName(nome);
            signature.setLocation("Assinado via API Kurzgesagtt");
            signature.setReason("Assinatura Digital");
            signature.setSignDate(Calendar.getInstance());
            signature.setContactInfo(email);

            document.addSignature(signature, new PdfBoxSignature(privateKey));
            document.saveIncremental(outputStream);

            return outputStream.toByteArray();
        }
    }

    private static void adicionarMetadados(PDDocument document, String nome, String email, String cpf) {
        PDDocumentInformation info = document.getDocumentInformation();
        
        // Metadados padrão
        info.setAuthor(nome);
        info.setTitle("Documento Assinado Digitalmente");
        info.setSubject("Assinatura Digital - " + nome);
        info.setCreator("API Kurzgesagtt");
        info.setProducer("PDFBox + Spring Boot");
        
        // Metadados customizados
        info.setCustomMetadataValue("Assinante_Nome", nome);
        info.setCustomMetadataValue("Assinante_Email", email);
        info.setCustomMetadataValue("Assinante_CPF", cpf);
        info.setCustomMetadataValue("Data_Assinatura", LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
        info.setCustomMetadataValue("Sistema", "API Kurzgesagtt v1.0");
        
        document.setDocumentInformation(info);
    }

    private static void adicionarPaginaDeAssinatura(PDDocument document, String nome, String email, String cpf,
                                                     String verificationCode, String verificationUrl, byte[] qrCodeBytes) throws IOException {
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float margin = 50;
            float yPosition = 750;
            
            // Título
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("CERTIFICADO DE ASSINATURA DIGITAL");
            contentStream.endText();
            
            yPosition -= 40;
            
            // Linha separadora
            contentStream.setLineWidth(2);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(550, yPosition);
            contentStream.stroke();
            
            yPosition -= 50;
            
            // Informações do signatário
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Informacoes do Signatario");
            contentStream.endText();
            
            yPosition -= 30;
            
            drawField(contentStream, margin, yPosition, "Nome Completo:", nome);
            yPosition -= 25;
            drawField(contentStream, margin, yPosition, "CPF:", formatarCPF(cpf));
            yPosition -= 25;
            drawField(contentStream, margin, yPosition, "E-mail para Contato:", email);
            
            yPosition -= 50;
            
            // Informações da assinatura
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Detalhes da Assinatura");
            contentStream.endText();
            
            yPosition -= 30;
            
            String dataHora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm:ss"));
            drawField(contentStream, margin, yPosition, "Data e Hora:", dataHora);
            yPosition -= 25;
            drawField(contentStream, margin, yPosition, "Metodo:", "Assinatura Digital (SHA256withRSA)");
            yPosition -= 25;
            drawField(contentStream, margin, yPosition, "Sistema:", "API Kurzgesagtt v1.0");
            
            yPosition -= 50;

            // QR Code de verificação
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Verificacao de Autenticidade");
            contentStream.endText();
            
            yPosition -= 20;
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 9);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Escaneie o QR Code ou acesse o link para verificar a autenticidade desta assinatura.");
            contentStream.endText();
            
            yPosition -= 15;

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 8);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Link: " + verificationUrl);
            contentStream.endText();

            yPosition -= 10;

            // Desenhar QR Code
            if (qrCodeBytes != null && qrCodeBytes.length > 0) {
                PDImageXObject qrImage = PDImageXObject.createFromByteArray(document, qrCodeBytes, "qrcode.png");
                float qrSize = 120;
                contentStream.drawImage(qrImage, margin, yPosition - qrSize, qrSize, qrSize);
                yPosition -= (qrSize + 15);
            }
            
            yPosition -= 20;

            // Linha separadora
            contentStream.setLineWidth(1);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(550, yPosition);
            contentStream.stroke();
            
            yPosition -= 25;
            
            // Aviso legal
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("AVISO LEGAL");
            contentStream.endText();
            
            yPosition -= 20;
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 9);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Este documento foi assinado digitalmente e contem certificado de autenticidade.");
            contentStream.endText();
            
            yPosition -= 15;
            
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 9);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("A assinatura digital garante a integridade e autenticidade do documento.");
            contentStream.endText();
            
            // Rodapé
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 8);
            contentStream.newLineAtOffset(margin, 30);
            contentStream.showText("Documento gerado automaticamente em " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
            contentStream.endText();
            
            contentStream.close();
        }
    }

    private static void drawField(PDPageContentStream cs, float margin, float y, String label, String value) throws IOException {
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA_BOLD, 12);
        cs.newLineAtOffset(margin, y);
        cs.showText(label);
        cs.endText();
        cs.beginText();
        cs.setFont(PDType1Font.HELVETICA, 12);
        cs.newLineAtOffset(margin + 150, y);
        cs.showText(value != null ? value : "");
        cs.endText();
    }
    
    private static String formatarCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return cpf;
        }
        return cpf.substring(0, 3) + "." + cpf.substring(3, 6) + "." + 
               cpf.substring(6, 9) + "-" + cpf.substring(9, 11);
    }

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
