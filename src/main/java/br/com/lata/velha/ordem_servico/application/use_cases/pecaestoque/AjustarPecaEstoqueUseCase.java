package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.application.dtos.request.AjustarPecaEstoqueRequest;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.shared.application.logging.Logger;

public class AjustarPecaEstoqueUseCase {

    private final AjustarPecaEstoqueGateway gateway;
    private final Logger logger;

    public AjustarPecaEstoqueUseCase(AjustarPecaEstoqueGateway gateway, Logger logger) {
        this.gateway = gateway;
        this.logger = logger;
    }

    public PecaEstoque execute(Long pecaId, AjustarPecaEstoqueRequest request) {
        logger.logInfo("Ajustando estoque de peça - pecaId={}, quantidadeArmazenada={}, quantidadeDisponivel={}",
                pecaId, request.quantidadeArmazenada(), request.quantidadeDisponivel());
        gateway.getPecaAtivaPorId(pecaId);
        var estoque = gateway.getEstoquePorPecaId(pecaId);
        estoque.ajustar(request.quantidadeArmazenada(), request.quantidadeDisponivel());

        logger.logInfo("Salvando estoque ajustado - pecaId={}", pecaId);
        var saved = gateway.salvarEstoque(estoque);
        logger.logInfo("Estoque de peça ajustado com sucesso - pecaId={}, quantidadeArmazenada={}, quantidadeDisponivel={}",
                pecaId, saved.getQuantidadeArmazenada(), saved.getQuantidadeDisponivel());
        return saved;
    }
}
