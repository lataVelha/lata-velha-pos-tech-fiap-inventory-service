package br.com.lata.velha.ordem_servico.infrastructure.persistence.gateways;

import br.com.lata.velha.ordem_servico.application.services.pecaalocada.NotificarAdminEncomendaPecaGateway;
import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.domain.repositories.PecaRepository;
import br.com.lata.velha.shared.application.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Inventory não tem dados de Funcionario/User (esses ficam no OsService), então os
 * destinatários admin vêm de uma lista estática em config por enquanto.
 * TODO: substituir por uma chamada real ao OsService (ou um read-model sincronizado por
 * evento) quando a integração entre serviços existir.
 */
@Component
@RequiredArgsConstructor
public class NotificarAdminGatewayImpl implements NotificarAdminEncomendaPecaGateway {

    private final PecaRepository pecaRepository;
    private final Logger logger;

    @Value("${notificacoes.admin-emails:}")
    private String adminEmailsConfig;

    @Override
    public Optional<Peca> findPecaPorId(Long id) {
        try {
            return Optional.of(pecaRepository.getActiveById(id));
        } catch (Exception e) {
            logger.logError("Falha ao buscar peça para notificação de encomenda - pecaId=" + id, e);
            return Optional.empty();
        }
    }

    @Override
    public List<String> getAdminEmails() {
        if (adminEmailsConfig == null || adminEmailsConfig.isBlank()) {
            return List.of();
        }
        return Arrays.stream(adminEmailsConfig.split(","))
                .map(String::trim)
                .filter(email -> !email.isEmpty())
                .toList();
    }
}
