package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.events.DeducaoEstoqueFalhouEvent;
import br.com.lata.velha.ordem_servico.domain.events.ServiceEndedEvent;
import br.com.lata.velha.shared.application.gateways.EventPublisher;
import br.com.lata.velha.shared.application.logging.Logger;

public class ReceberServiceEndedUseCase {

    private final DeduzirEstoqueExecucaoUseCase deduzirEstoqueExecucaoUseCase;
    private final ReceberServiceEndedGateway gateway;
    private final EventPublisher eventPublisher;
    private final Logger logger;

    public ReceberServiceEndedUseCase(DeduzirEstoqueExecucaoUseCase deduzirEstoqueExecucaoUseCase,
                                      ReceberServiceEndedGateway gateway,
                                      EventPublisher eventPublisher,
                                      Logger logger) {
        this.deduzirEstoqueExecucaoUseCase = deduzirEstoqueExecucaoUseCase;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
        this.logger = logger;
    }

    public void execute(ServiceEndedEvent event) {
        logger.logInfo("Processando ServiceEnded - osId={}, execucaoServicoId={}", event.getOsId(), event.getExecucaoServicoId());

        var itensPendentes = event.getItens().stream()
                .filter(item -> !jaInstalada(event.getExecucaoServicoId(), item.pecaId()))
                .toList();

        if (itensPendentes.isEmpty()) {
            logger.logInfo("ServiceEnded já processado anteriormente - execucaoServicoId={}", event.getExecucaoServicoId());
            return;
        }

        try {
            var input = new DeduzirEstoqueExecucaoUseCase.Input(event.getExecucaoServicoId(), itensPendentes.stream()
                    .map(item -> new DeduzirEstoqueExecucaoUseCase.Input.Item(item.pecaId(), item.quantidade()))
                    .toList());
            deduzirEstoqueExecucaoUseCase.execute(input);

            for (var item : itensPendentes) {
                gateway.buscarPecaAlocadaExistente(event.getExecucaoServicoId(), item.pecaId())
                        .ifPresent(pecaAlocada -> {
                            pecaAlocada.instalar(item.quantidade());
                            gateway.salvarPecaAlocada(pecaAlocada);
                        });
            }
            logger.logInfo("ServiceEnded processado com sucesso - execucaoServicoId={}", event.getExecucaoServicoId());
        } catch (IllegalArgumentException e) {
            logger.logError("Falha ao deduzir estoque para ServiceEnded - osId=" + event.getOsId() + ", execucaoServicoId=" + event.getExecucaoServicoId(), e);
            eventPublisher.publish(new DeducaoEstoqueFalhouEvent(event.getOsId(), event.getExecucaoServicoId(), e.getMessage()));
        }
    }

    private boolean jaInstalada(Long execucaoServicoId, Long pecaId) {
        return gateway.buscarPecaAlocadaExistente(execucaoServicoId, pecaId)
                .map(pecaAlocada -> pecaAlocada.isInstalada())
                .orElse(false);
    }
}
