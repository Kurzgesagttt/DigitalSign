package com.Kurzgesagtt.DigitalSignature.controller;

import com.Kurzgesagtt.DigitalSignature.services.KeyGenerator;
import com.Kurzgesagtt.DigitalSignature.services.PdfMemorySignService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.KeyPair;

@RestController
@RequestMapping("/v1")
public class PdfSignController {

    @Autowired
    PdfMemorySignService signService;

    @GetMapping("/sign")
    public String index() {
        return "Use o método POST para enviar o PDF a ser assinado.";
    }

    @PostMapping("/sign")
    public ResponseEntity<byte[]> signPdf(
            @RequestParam("file") MultipartFile file,
            @RequestParam("nome") String nome,
            @RequestParam("email") String email,
            @RequestParam("cpf") String cpf
    ) throws Exception {

        KeyPair keyPair = KeyGenerator.generateKeyPair();
        byte[] signedPdf = PdfMemorySignService.signPdf(
                file.getBytes(),
                keyPair.getPrivate(),
                nome,
                email,
                cpf
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "documento_assinado.pdf");

        return new ResponseEntity<>(signedPdf, headers, HttpStatus.OK);
    }
}
