package br.com.lata.velha.ordem_servico.application.use_cases.peca;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.application.logging.Logger;

public class BuscarPecaPorIdUseCase {

    private final BuscarPecaPorIdGateway gateway;
    private final Logger logger;

    public BuscarPecaPorIdUseCase(BuscarPecaPorIdGateway gateway, Logger logger) {
        this.gateway = gateway;
        this.logger = logger;
    }

    public Peca execute(Long id) {
        logger.logInfo("Buscando peça por id - pecaId={}", id);
        return gateway.getPecaAtivaPorId(id);
    }
}
