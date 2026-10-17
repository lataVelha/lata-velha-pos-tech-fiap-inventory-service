package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.application.dtos.request.MovimentarPecaEstoqueRequest;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.shared.application.logging.Logger;

public class SaidaPecaEstoqueUseCase {

    private final SaidaPecaEstoqueGateway gateway;
    private final Logger logger;

    public SaidaPecaEstoqueUseCase(SaidaPecaEstoqueGateway gateway, Logger logger) {
        this.gateway = gateway;
        this.logger = logger;
    }

    public PecaEstoque execute(Long pecaId, MovimentarPecaEstoqueRequest request) {
        logger.logInfo("Registrando saída de estoque - pecaId={}, quantidade={}", pecaId, request.quantidade());
        gateway.getPecaAtivaPorId(pecaId);
        var estoque = gateway.getEstoquePorPecaId(pecaId);
        if (estoque.getQuantidadeArmazenada() < request.quantidade()) {
            logger.logWarn("Saída de estoque rejeitada: estoque insuficiente - pecaId={}, quantidadeArmazenada={}, quantidadeSolicitada={}",
                    pecaId, estoque.getQuantidadeArmazenada(), request.quantidade());
        }
        estoque.retirar(request.quantidade());

        logger.logInfo("Salvando estoque atualizado - pecaId={}", pecaId);
        var saved = gateway.salvarEstoque(estoque);
        logger.logInfo("Saída de estoque registrada com sucesso - pecaId={}, quantidadeArmazenada={}", pecaId, saved.getQuantidadeArmazenada());
        return saved;
    }
}
