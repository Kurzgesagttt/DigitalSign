package com.Kurzgesagtt.DigitalSignature.config;

import com.Kurzgesagtt.DigitalSignature.model.UserRole;
import com.Kurzgesagtt.DigitalSignature.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminBootstrapConfig {

    @Bean
    public ApplicationRunner promoteBootstrapAdmin(
            UsuarioRepository usuarioRepository,
            @Value("${app.admin.bootstrap-email:}") String bootstrapEmail
    ) {
        return args -> {
            if (bootstrapEmail == null || bootstrapEmail.isBlank()) {
                return;
            }

            usuarioRepository.findByEmail(bootstrapEmail.trim().toLowerCase()).ifPresent(usuario -> {
                if (usuario.getRole() != UserRole.ADMIN) {
                    usuario.setRole(UserRole.ADMIN);
                    usuarioRepository.save(usuario);
                }
            });
        };
    }
}
