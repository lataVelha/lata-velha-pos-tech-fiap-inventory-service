package br.com.lata.velha.ordem_servico.application.use_cases.peca;

import br.com.lata.velha.ordem_servico.application.dtos.request.AtualizarPecaRequest;
import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.application.logging.Logger;

public class AtualizarPecaUseCase {

    private final AtualizarPecaGateway gateway;
    private final Logger logger;

    public AtualizarPecaUseCase(AtualizarPecaGateway gateway, Logger logger) {
        this.gateway = gateway;
        this.logger = logger;
    }

    public Peca execute(Long id, AtualizarPecaRequest request) {
        logger.logInfo("Buscando peça para atualização - pecaId={}", id);
        Peca peca = gateway.getPecaAtivaPorId(id);
        peca.atualizar(request.nome(), request.descricao(), request.valor());

        logger.logInfo("Salvando peça atualizada - pecaId={}", id);
        var updated = gateway.salvarPeca(peca);
        logger.logInfo("Peça atualizada com sucesso - pecaId={}", id);
        return updated;
    }
}
