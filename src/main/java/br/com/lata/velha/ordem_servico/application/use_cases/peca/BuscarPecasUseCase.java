package br.com.lata.velha.ordem_servico.application.use_cases.peca;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.application.logging.Logger;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;

public class BuscarPecasUseCase {

    private final BuscarPecasGateway gateway;
    private final Logger logger;

    public BuscarPecasUseCase(BuscarPecasGateway gateway, Logger logger) {
        this.gateway = gateway;
        this.logger = logger;
    }

    public PaginatedResult<Peca> execute(int page, int size) {
        logger.logInfo("Buscando peças - page={}, size={}", page, size);
        return gateway.findAll(page, size);
    }
}
