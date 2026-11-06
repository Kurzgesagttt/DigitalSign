package com.Kurzgesagtt.DigitalSignature.services;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitServiceTest {

    @Test
    void loginLimitRejectsRequestsAfterConfiguredThreshold() {
        RequestMetadataService metadataService = new RequestMetadataService();
        RateLimitService service = new RateLimitService(
                metadataService,
                2,
                10,
                10,
                Clock.fixed(Instant.parse("2026-11-05T12:00:00Z"), ZoneOffset.UTC)
        );
        HttpServletRequest request = requestFrom("192.0.2.10");

        assertThat(service.allowLogin(request)).isTrue();
        assertThat(service.allowLogin(request)).isTrue();
        assertThat(service.allowLogin(request)).isFalse();
    }

    private HttpServletRequest requestFrom(String ipAddress) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRemoteAddr(ipAddress);
        return request;
    }
}
