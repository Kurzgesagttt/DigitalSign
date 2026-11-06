package com.Kurzgesagtt.DigitalSignature.services;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class RateLimitService {
    private static final long WINDOW_MILLIS = 60_000;

    private final RequestMetadataService requestMetadataService;
    private final int loginLimit;
    private final int verificationLimit;
    private final int signingLimit;
    private final Clock clock;
    private final ConcurrentHashMap<String, Counter> counters = new ConcurrentHashMap<>();

    @Autowired
    public RateLimitService(
            RequestMetadataService requestMetadataService,
            @Value("${app.rate-limit.login-per-minute:10}") int loginLimit,
            @Value("${app.rate-limit.verification-per-minute:30}") int verificationLimit,
            @Value("${app.rate-limit.signing-per-minute:10}") int signingLimit
    ) {
        this(requestMetadataService, loginLimit, verificationLimit, signingLimit, Clock.systemUTC());
    }

    RateLimitService(RequestMetadataService requestMetadataService, int loginLimit, int verificationLimit, int signingLimit, Clock clock) {
        this.requestMetadataService = requestMetadataService;
        this.loginLimit = loginLimit;
        this.verificationLimit = verificationLimit;
        this.signingLimit = signingLimit;
        this.clock = clock;
    }

    public boolean allowLogin(HttpServletRequest request) {
        return allow("login", loginLimit, request);
    }

    public boolean allowVerification(HttpServletRequest request) {
        return allow("verification", verificationLimit, request);
    }

    public boolean allowSigning(HttpServletRequest request) {
        return allow("signing", signingLimit, request);
    }

    public boolean allowAccountDeletion(Long userId) {
        return allowKey("account-deletion:" + userId, loginLimit);
    }

    private boolean allow(String action, int limit, HttpServletRequest request) {
        return allowKey(action + ':' + requestMetadataService.getClientIp(request), limit);
    }

    private boolean allowKey(String key, int limit) {
        long now = clock.millis();
        Counter counter = counters.compute(key, (ignored, current) -> {
            if (current == null || now - current.startedAt >= WINDOW_MILLIS) {
                return new Counter(now);
            }
            return current;
        });
        return counter.attempts.incrementAndGet() <= limit;
    }

    private static final class Counter {
        private final long startedAt;
        private final AtomicInteger attempts = new AtomicInteger();

        private Counter(long startedAt) {
            this.startedAt = startedAt;
        }
    }
}
