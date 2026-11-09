package com.Kurzgesagtt.DigitalSignature.services;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.Security;

@Service
public class KeyGenerator {
    static {
        Security.addProvider(new BouncyCastleProvider());
    }
    // Gera criptografia assímetrica em memória.
    public static KeyPair generateKeyPair() throws Exception{
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA", "BC");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }
}
