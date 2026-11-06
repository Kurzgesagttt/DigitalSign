package com.Kurzgesagtt.DigitalSignature.services;

import com.Kurzgesagtt.DigitalSignature.model.Usuario;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountService {
    private final EntityManager entityManager;
    private final PasswordEncoder passwordEncoder;

    public AccountService(EntityManager entityManager, PasswordEncoder passwordEncoder) {
        this.entityManager = entityManager;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public Profile profile(Long userId) {
        Usuario user = requireUser(userId, LockModeType.NONE);
        long total = entityManager.createQuery("select count(s) from SignatureRecord s where s.usuario.id = :id", Long.class)
                .setParameter("id", userId).getSingleResult();
        return new Profile(user.getId(), user.getNomeCompleto(), user.getEmail(), user.getTelefone(),
                user.getCpf(), user.getDataNascimento(), user.getRole().name(), user.getCriadoEm(), total,
                List.of(
                        new DataCategory("Cadastro", List.of("nomeCompleto", "email", "telefone", "cpf", "dataNascimento", "criadoEm"),
                                "Nome, email e CPF identificam a conta e o signatario. Email permite o login. Telefone e data de nascimento sao mantidos no cadastro, sem uso adicional implementado."),
                        new DataCategory("Autenticacao e administracao", List.of("hash da senha", "role", "bloqueado", "bloqueadoMotivo", "bloqueadoPorUsuarioId", "bloqueadoEm"),
                                "Verificar a senha, controlar permissoes e aplicar bloqueios administrativos. O hash da senha nao e retornado neste perfil."),
                        new DataCategory("Assinaturas", List.of("nome, email e CPF do signatario", "codigo de verificacao", "data e status", "hashes SHA-256 do PDF original e assinado", "IP", "User-Agent", "dados de revogacao"),
                                "Registrar autoria declarada pela conta, permitir consulta do status pelo codigo e administrar revogacoes. O PDF e processado em memoria; seu conteudo nao e gravado no banco pela aplicacao."),
                        new DataCategory("Auditoria", List.of("conta, email e perfil do ator", "evento e resultado", "tipo e identificador do alvo", "IP", "User-Agent", "detalhes", "data"),
                                "Registrar cadastro, tentativas de login, assinaturas, consultas de verificacao e acoes administrativas para acompanhamento operacional.")
                ), new DeletionInfo(
                        "A exclusao remove a conta, seus registros de assinatura e auditorias vinculadas no banco da aplicacao. Os codigos dessas assinaturas deixam de ser localizados. Esta operacao e irreversivel.",
                        List.of("PDFs ja baixados ou compartilhados permanecem com seus destinatarios.",
                                "Esta operacao nao apaga copias em backups ou registros externos da hospedagem; sua gestao depende do operador.",
                                "Referencias administrativas ao identificador desta conta em outros registros sao desvinculadas; os demais registros dessas contas permanecem.")));
    }

    @Transactional
    public boolean delete(Long userId, String password) {
        Usuario user = requireUser(userId, LockModeType.PESSIMISTIC_WRITE);
        // BCrypt only accepts passwords up to 72 bytes. Reject oversized input without a server error.
        if (password.getBytes(StandardCharsets.UTF_8).length > 72 || !passwordEncoder.matches(password, user.getSenha())) {
            return false;
        }
        // Delete audit snapshots as well as foreign keys, including public verification events.
        entityManager.createQuery("""
                delete from AuditLog a where a.actor.id = :id or a.actorEmail = :email
                or (a.targetType = 'USUARIO' and a.targetId = :id)
                or (a.targetType = 'SIGNATURE' and a.targetId in
                    (select s.id from SignatureRecord s where s.usuario.id = :id))
                """).setParameter("id", userId).setParameter("email", user.getEmail()).executeUpdate();
        entityManager.createQuery("delete from SignatureRecord s where s.usuario.id = :id")
                .setParameter("id", userId).executeUpdate();
        entityManager.createQuery("update SignatureRecord s set s.revogadoPorUsuarioId = null where s.revogadoPorUsuarioId = :id")
                .setParameter("id", userId).executeUpdate();
        entityManager.createQuery("update Usuario u set u.bloqueadoPorUsuarioId = null where u.bloqueadoPorUsuarioId = :id")
                .setParameter("id", userId).executeUpdate();
        entityManager.remove(user);
        entityManager.flush();
        return true;
    }

    private Usuario requireUser(Long userId, LockModeType lock) {
        Usuario user = entityManager.find(Usuario.class, userId, lock);
        if (user == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Conta nao encontrada.");
        return user;
    }

    public record DataCategory(String categoria, List<String> campos, String finalidade) {}
    public record DeletionInfo(String descricao, List<String> limitacoes) {}
    public record Profile(Long id, String nomeCompleto, String email, String telefone, String cpf,
                          LocalDate dataNascimento, String role, LocalDateTime criadoEm, long totalAssinaturas,
                          List<DataCategory> categoriasDados, DeletionInfo exclusao) {}
}
