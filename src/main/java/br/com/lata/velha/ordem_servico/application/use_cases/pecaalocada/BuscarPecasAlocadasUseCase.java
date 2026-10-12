package br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.shared.application.logging.Logger;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;

public class BuscarPecasAlocadasUseCase {

    private final BuscarPecasAlocadasGateway gateway;
    private final Logger logger;

    public BuscarPecasAlocadasUseCase(BuscarPecasAlocadasGateway gateway, Logger logger) {
        this.gateway = gateway;
        this.logger = logger;
    }

    public PaginatedResult<PecaAlocada> execute(Long servicoOsId, int page, int size) {
        logger.logInfo("Buscando peças alocadas - execucaoServicoId={}, page={}, size={}", servicoOsId, page, size);
        return gateway.findByExecucaoServicoId(servicoOsId, page, size);
    }
}
