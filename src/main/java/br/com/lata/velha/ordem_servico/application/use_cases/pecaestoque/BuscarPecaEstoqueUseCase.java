package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.shared.application.logging.Logger;

public class BuscarPecaEstoqueUseCase {

    private final BuscarPecaEstoqueGateway gateway;
    private final Logger logger;

    public BuscarPecaEstoqueUseCase(BuscarPecaEstoqueGateway gateway, Logger logger) {
        this.gateway = gateway;
        this.logger = logger;
    }

    public PecaEstoque execute(Long pecaId) {
        logger.logInfo("Buscando estoque de peça - pecaId={}", pecaId);
        gateway.getPecaAtivaPorId(pecaId);
        return gateway.getEstoquePorPecaId(pecaId);
    }
}
