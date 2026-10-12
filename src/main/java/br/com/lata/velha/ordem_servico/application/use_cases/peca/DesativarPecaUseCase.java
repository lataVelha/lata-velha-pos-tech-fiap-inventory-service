package br.com.lata.velha.ordem_servico.application.use_cases.peca;

import br.com.lata.velha.shared.application.logging.Logger;

public class DesativarPecaUseCase {

    private final DesativarPecaGateway gateway;
    private final Logger logger;

    public DesativarPecaUseCase(DesativarPecaGateway gateway, Logger logger) {
        this.gateway = gateway;
        this.logger = logger;
    }

    public void execute(Long id) {
        logger.logInfo("Buscando peça para desativação - pecaId={}", id);
        var peca = gateway.getPecaAtivaPorId(id);
        peca.desativar();

        logger.logInfo("Salvando desativação da peça - pecaId={}", id);
        gateway.salvarPeca(peca);
        logger.logInfo("Peça desativada com sucesso - pecaId={}", id);
    }
}
