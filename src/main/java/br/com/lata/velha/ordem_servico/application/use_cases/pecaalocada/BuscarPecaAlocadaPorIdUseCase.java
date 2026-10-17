package br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.shared.application.logging.Logger;

public class BuscarPecaAlocadaPorIdUseCase {

    private final BuscarPecaAlocadaPorIdGateway gateway;
    private final Logger logger;

    public BuscarPecaAlocadaPorIdUseCase(BuscarPecaAlocadaPorIdGateway gateway, Logger logger) {
        this.gateway = gateway;
        this.logger = logger;
    }

    public PecaAlocada execute(Long id) {
        logger.logInfo("Buscando peça alocada por id - pecaAlocadaId={}", id);
        return gateway.getPecaAlocadaPorId(id);
    }
}
