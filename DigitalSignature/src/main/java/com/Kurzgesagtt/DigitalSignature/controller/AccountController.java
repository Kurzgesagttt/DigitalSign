package com.Kurzgesagtt.DigitalSignature.controller;

import com.Kurzgesagtt.DigitalSignature.services.AccountService;
import com.Kurzgesagtt.DigitalSignature.services.AuthenticatedUserService;
import com.Kurzgesagtt.DigitalSignature.services.RateLimitService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/v1/users/me")
public class AccountController {
    private final AccountService accounts;
    private final AuthenticatedUserService authenticatedUser;
    private final RateLimitService rateLimits;

    public AccountController(AccountService accounts, AuthenticatedUserService authenticatedUser, RateLimitService rateLimits) {
        this.accounts = accounts;
        this.authenticatedUser = authenticatedUser;
        this.rateLimits = rateLimits;
    }

    @GetMapping
    public ResponseEntity<?> profile() {
        return ResponseEntity.ok().header("Cache-Control", "no-store")
                .body(accounts.profile(authenticatedUser.getCurrentUser().getId()));
    }

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestBody DeleteRequest body) {
        Long userId = authenticatedUser.getCurrentUser().getId();
        if (!rateLimits.allowAccountDeletion(userId)) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body(Map.of("erro", "Muitas tentativas. Tente novamente mais tarde."));
        }
        if (body.senha == null || body.senha.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Informe a senha atual para excluir a conta."));
        }
        if (!accounts.delete(userId, body.senha)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("erro", "Senha atual incorreta."));
        }
        return ResponseEntity.noContent().build();
    }

    public static class DeleteRequest {
        public String senha;
    }
}
