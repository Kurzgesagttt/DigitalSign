package com.Kurzgesagtt.DigitalSignature.services;

import org.apache.pdfbox.io.IOUtils;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.SignerInfoGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Service
public class PdfMemorySignService {

    public static byte[] signPdf(byte[] pdfBytes, KeyPair keyPair, String verificationCode, String verificationUrl, byte[] qrCodeBytes) throws IOException {
        byte[] modifiedPdfBytes;
        try (PDDocument document = PDDocument.load(pdfBytes);
             ByteArrayOutputStream tempStream = new ByteArrayOutputStream()) {
            adicionarMetadados(document);
            adicionarPaginaDeAssinatura(document, verificationCode, verificationUrl, qrCodeBytes);
            document.save(tempStream);
            modifiedPdfBytes = tempStream.toByteArray();
        }

        try (PDDocument document = PDDocument.load(modifiedPdfBytes);
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("DigitalSign demonstracao");
            signature.setLocation("DigitalSign API");
            signature.setReason("Assinatura digital demonstrativa");
            signature.setSignDate(Calendar.getInstance());
            signature.setContactInfo("Verificacao pelo codigo do documento");

            document.addSignature(signature, new PdfBoxSignature(keyPair));
            document.saveIncremental(outputStream);
            return outputStream.toByteArray();
        }
    }

    private static void adicionarMetadados(PDDocument document) {
        PDDocumentInformation info = document.getDocumentInformation();
        info.setAuthor("DigitalSign");
        info.setTitle("Documento Assinado Digitalmente");
        info.setSubject("Assinatura digital demonstrativa");
        info.setCreator("DigitalSign API");
        info.setProducer("PDFBox + Spring Boot");
        document.setDocumentInformation(info);
    }

    //pagina extra com dados da assinatura.
    private static void adicionarPaginaDeAssinatura(PDDocument document, String verificationCode, String verificationUrl, byte[] qrCodeBytes) throws IOException {
        PDPage page = new PDPage();
        document.addPage(page);

        try (PDPageContentStream contentStream = new PDPageContentStream(document, page)) {
            float margin = 50;
            float yPosition = 750;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 24);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("CERTIFICADO DE ASSINATURA DIGITAL");
            contentStream.endText();

            yPosition -= 40;
            contentStream.setLineWidth(2);
            contentStream.moveTo(margin, yPosition);
            contentStream.lineTo(550, yPosition);
            contentStream.stroke();

            yPosition -= 50;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Informacoes de Verificacao");
            contentStream.endText();

            yPosition -= 30;
            drawField(contentStream, margin, yPosition, "Codigo:", verificationCode);
            yPosition -= 25;
            drawField(contentStream, margin, yPosition, "Finalidade:", "Verificacao do registro na API");

            yPosition -= 50;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 16);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Detalhes da Assinatura");
            contentStream.endText();

            yPosition -= 30;
            drawField(contentStream, margin, yPosition, "Data e hora (UTC):", DateTimeFormatter.ISO_INSTANT.format(Instant.now()));
            yPosition -= 25;
            drawField(contentStream, margin, yPosition, "Metodo:", "CMS SHA256withRSA");

            yPosition -= 50;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Verificacao de Integridade");
            contentStream.endText();

            yPosition -= 20;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 9);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Escaneie o QR Code ou acesse o link para consultar o registro.");
            contentStream.endText();

            yPosition -= 15;
            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 8);
            contentStream.newLineAtOffset(margin, yPosition);
            contentStream.showText("Link: " + verificationUrl);
            contentStream.endText();

            yPosition -= 10;
            if (qrCodeBytes != null && qrCodeBytes.length > 0) {
                PDImageXObject qrImage = PDImageXObject.createFromByteArray(document, qrCodeBytes, "qrcode.png");
                float qrSize = 120;
                contentStream.drawImage(qrImage, margin, yPosition - qrSize, qrSize, qrSize);
                yPosition -= qrSize + 15;
            }

            contentStream.beginText();
            contentStream.setFont(PDType1Font.HELVETICA, 8);
            contentStream.newLineAtOffset(margin, 30);
            contentStream.showText("Certificado efemero de demonstracao; nao representa ICP-Brasil ou identidade externa.");
            contentStream.endText();
        }
    }

    private static void drawField(PDPageContentStream contentStream, float margin, float y, String label, String value) throws IOException {
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12);
        contentStream.newLineAtOffset(margin, y);
        contentStream.showText(label);
        contentStream.endText();
        contentStream.beginText();
        contentStream.setFont(PDType1Font.HELVETICA, 12);
        contentStream.newLineAtOffset(margin + 150, y);
        contentStream.showText(value != null ? value : "");
        contentStream.endText();
    }

    private static class PdfBoxSignature implements SignatureInterface {
        private final KeyPair keyPair;

        private PdfBoxSignature(KeyPair keyPair) {
            this.keyPair = keyPair;
        }

        @Override
        public byte[] sign(InputStream content) throws IOException {
            try {
                X509Certificate certificate = createEphemeralCertificate(keyPair);
                ContentSigner signer = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(keyPair.getPrivate());
                SignerInfoGenerator signerInfo = new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder().setProvider("BC").build()
                ).build(signer, certificate);
                CMSSignedDataGenerator generator = new CMSSignedDataGenerator();
                generator.addSignerInfoGenerator(signerInfo);
                generator.addCertificates(new JcaCertStore(List.of(certificate)));
                return generator.generate(new CMSProcessableByteArray(IOUtils.toByteArray(content)), false).getEncoded();
            } catch (Exception exception) {
                throw new IOException("Erro ao gerar assinatura CMS do PDF", exception);
            }
        }

        private X509Certificate createEphemeralCertificate(KeyPair pair) throws Exception {
            Instant now = Instant.now();
            X500Name subject = new X500Name("CN=DigitalSign Demonstration");
            X509CertificateHolder holder = new JcaX509v3CertificateBuilder(
                    subject,
                    new BigInteger(160, new SecureRandom()),
                    Date.from(now.minusSeconds(60)),
                    Date.from(now.plusSeconds(300)),
                    subject,
                    pair.getPublic()
            ).build(new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(pair.getPrivate()));
            return new JcaX509CertificateConverter().setProvider("BC").getCertificate(holder);
        }
    }
}
