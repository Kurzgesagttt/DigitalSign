package com.Kurzgesagtt.DigitalSignature.security;

import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

/**
 * Rate limiter simples em memória para tentativas de login.
 * Permite no máximo MAX_ATTEMPTS falhas em WINDOW_SECONDS por email.
 * Após atingir o limite, bloqueia por WINDOW_SECONDS.
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long WINDOW_SECONDS = 15 * 60; // 15 minutos

    private record Bucket(int attempts, Instant windowStart) {}

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    /** Retorna true se a tentativa está bloqueada (limite excedido). */
    public boolean isBlocked(String email) {
        Bucket bucket = buckets.get(normalizar(email));
        if (bucket == null) return false;

        if (isExpirado(bucket)) {
            buckets.remove(normalizar(email));
            return false;
        }

        return bucket.attempts() >= MAX_ATTEMPTS;
    }

    /** Registra uma falha de autenticação. */
    public void registrarFalha(String email) {
        String key = normalizar(email);
        buckets.compute(key, (k, existing) -> {
            if (existing == null || isExpirado(existing)) {
                return new Bucket(1, Instant.now());
            }
            return new Bucket(existing.attempts() + 1, existing.windowStart());
        });
    }

    /** Limpa o contador após login bem-sucedido. */
    public void registrarSucesso(String email) {
        buckets.remove(normalizar(email));
    }

    /** Segundos restantes de bloqueio (0 se não bloqueado). */
    public long segundosRestantes(String email) {
        Bucket bucket = buckets.get(normalizar(email));
        if (bucket == null || isExpirado(bucket)) return 0;
        long restante = WINDOW_SECONDS - (Instant.now().getEpochSecond() - bucket.windowStart().getEpochSecond());
        return Math.max(0, restante);
    }

    private boolean isExpirado(Bucket bucket) {
        return Instant.now().getEpochSecond() - bucket.windowStart().getEpochSecond() >= WINDOW_SECONDS;
    }

    private String normalizar(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
