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

//Esse controller está disponível apenas para testes locais, sem assinaturas válidas.
@RestController
@RequestMapping("/v1")
public class PdfSignController {

    @Autowired
    PdfMemorySignService signService;

    @GetMapping("/sign")
    public String index() {
        return "talvez você esteja utilizando o metodo GET ao inves de POST";
    }

    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/sign")
    public ResponseEntity<byte[]> signPdf(@RequestParam("file")MultipartFile file) throws Exception {
        KeyPair keyPair = KeyGenerator.generateKeyPair();
        byte[] signedPdf = PdfMemorySignService.signPdf(file.getBytes(), keyPair.getPrivate());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "signed.pdf"); // ou inline para visualizar no navegador

        return new ResponseEntity<>(signedPdf, headers, HttpStatus.OK);
    }
}
