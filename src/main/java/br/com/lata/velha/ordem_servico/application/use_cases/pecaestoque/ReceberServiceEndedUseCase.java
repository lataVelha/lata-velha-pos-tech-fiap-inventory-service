package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.events.DeducaoEstoqueFalhouEvent;
import br.com.lata.velha.ordem_servico.domain.events.ServiceEndedEvent;
import br.com.lata.velha.shared.application.gateways.EventPublisher;
import br.com.lata.velha.shared.application.logging.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

        var alocadasPorPecaId = new LinkedHashMap<Long, PecaAlocada>();
        var naoReservadas = new ArrayList<Long>();
        for (var item : event.getItens()) {
            var alocada = gateway.buscarPecaAlocadaExistente(event.getExecucaoServicoId(), item.pecaId());
            if (alocada.isEmpty()) {
                naoReservadas.add(item.pecaId());
            } else {
                alocadasPorPecaId.put(item.pecaId(), alocada.get());
            }
        }

        if (!naoReservadas.isEmpty()) {
            var motivo = "Peças sem reserva registrada para a execução: " + naoReservadas;
            logger.logError("Dedução de estoque recusada, peças nunca reservadas - osId={}, execucaoServicoId={}, pecaIds={}",
                    event.getOsId(), event.getExecucaoServicoId(), naoReservadas);
            eventPublisher.publish(new DeducaoEstoqueFalhouEvent(event.getOsId(), event.getExecucaoServicoId(), motivo));
            return;
        }

        var itensPendentes = event.getItens().stream()
                .filter(item -> !alocadasPorPecaId.get(item.pecaId()).isInstalada())
                .toList();

        if (itensPendentes.isEmpty()) {
            logger.logInfo("ServiceEnded já processado anteriormente - execucaoServicoId={}", event.getExecucaoServicoId());
            return;
        }

        try {
            deduzirEstoqueExecucaoUseCase.execute(toDeducaoInput(event, itensPendentes));
            marcarInstaladas(itensPendentes, alocadasPorPecaId);
            logger.logInfo("ServiceEnded processado com sucesso - execucaoServicoId={}", event.getExecucaoServicoId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.logError("Falha ao deduzir estoque para ServiceEnded - osId=" + event.getOsId()
                    + ", execucaoServicoId=" + event.getExecucaoServicoId(), e);
            eventPublisher.publish(new DeducaoEstoqueFalhouEvent(event.getOsId(), event.getExecucaoServicoId(), e.getMessage()));
        }
    }

    private DeduzirEstoqueExecucaoUseCase.Input toDeducaoInput(ServiceEndedEvent event, List<ServiceEndedEvent.ItemPeca> itensPendentes) {
        var itens = itensPendentes.stream()
                .map(item -> new DeduzirEstoqueExecucaoUseCase.Input.Item(item.pecaId(), item.quantidade()))
                .collect(Collectors.toList());
        return new DeduzirEstoqueExecucaoUseCase.Input(event.getExecucaoServicoId(), itens);
    }

    private void marcarInstaladas(List<ServiceEndedEvent.ItemPeca> itensPendentes, Map<Long, PecaAlocada> alocadasPorPecaId) {
        for (var item : itensPendentes) {
            var pecaAlocada = alocadasPorPecaId.get(item.pecaId());
            pecaAlocada.instalar(item.quantidade());
            gateway.salvarPecaAlocada(pecaAlocada);
        }
    }
}
