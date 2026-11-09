package com.Kurzgesagtt.DigitalSignature.services;

import com.Kurzgesagtt.DigitalSignature.model.Usuario;
import com.Kurzgesagtt.DigitalSignature.repository.UsuarioRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UsuarioDetailsService implements UserDetailsService {
    private final UsuarioRepository usuarioRepository;

    public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @Override
    public UsuarioPrincipal loadUserByUsername(String email) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario nao encontrado."));

        return new UsuarioPrincipal(usuario);
    }

    public static class UsuarioPrincipal extends User {
        private final Long usuarioId;

        public UsuarioPrincipal(Usuario usuario) {
            super(usuario.getEmail(), usuario.getSenha(), !usuario.isBloqueado(), true, true, true,
                    List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRole().name())));
            this.usuarioId = usuario.getId();
        }

        public Long getUsuarioId() { return usuarioId; }
    }
}
