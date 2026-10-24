package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.application.dtos.request.MovimentarPecaEstoqueRequest;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import br.com.lata.velha.ordem_servico.domain.events.PecaCompletamenteAlocadaEvent;
import br.com.lata.velha.shared.application.gateways.EventPublisher;
import br.com.lata.velha.shared.application.logging.Logger;

public class EntradaPecaEstoqueUseCase {

    private final EntradaPecaEstoqueGateway gateway;
    private final EventPublisher eventPublisher;
    private final Logger logger;

    public EntradaPecaEstoqueUseCase(EntradaPecaEstoqueGateway gateway, EventPublisher eventPublisher, Logger logger) {
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
        this.logger = logger;
    }

    public PecaEstoque execute(Long pecaId, MovimentarPecaEstoqueRequest request) {
        logger.logInfo("Registrando entrada de estoque - pecaId={}, quantidade={}", pecaId, request.quantidade());
        gateway.getPecaAtivaPorId(pecaId);
        var estoque = gateway.getEstoquePorPecaId(pecaId);
        estoque.adicionar(request.quantidade());

        for (var pendente : gateway.buscarPendentesPorPeca(pecaId)) {
            pendente.reservar(estoque);
            gateway.salvarPecaAlocada(pendente);

            if (pendente.isReservada() || pendente.isInstalada()) {
                var todasDaExecucao = gateway.getTodasPecaAlocadaPorExecucaoServicoId(pendente.getExecucaoServicoId());
                var todasSatisfeitas = todasDaExecucao.stream()
                        .allMatch(p -> p.getStatus() == StatusPecaAlocada.RESERVADA || p.getStatus() == StatusPecaAlocada.INSTALADA);
                if (todasSatisfeitas) {
                    eventPublisher.publish(new PecaCompletamenteAlocadaEvent(pendente.getExecucaoServicoId()));
                }
            }
        }

        logger.logInfo("Salvando estoque atualizado - pecaId={}", pecaId);
        var saved = gateway.salvarEstoque(estoque);
        logger.logInfo("Entrada de estoque registrada com sucesso - pecaId={}, quantidadeArmazenada={}, quantidadeDisponivel={}",
                pecaId, saved.getQuantidadeArmazenada(), saved.getQuantidadeDisponivel());
        return saved;
    }
}
