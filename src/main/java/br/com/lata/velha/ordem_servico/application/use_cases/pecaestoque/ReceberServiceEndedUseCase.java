package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.shared.domain.events.DeducaoEstoqueFalhouEvent;
import br.com.lata.velha.shared.domain.events.ServiceEndedEvent;
import br.com.lata.velha.shared.application.gateways.EventPublisher;
import br.com.lata.velha.shared.application.logging.Logger;

import java.util.ArrayList;
import java.util.LinkedHashMap;
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

        var alocadasPorPecaId = new LinkedHashMap<String, PecaAlocada>();
        var naoReservadas = new ArrayList<String>();
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

        var quantidadesInvalidas = event.getItens().stream()
                .filter(item -> item.quantidade() == null || item.quantidade() <= 0)
                .map(ServiceEndedEvent.ItemPeca::pecaId)
                .toList();

        if (!quantidadesInvalidas.isEmpty()) {
            var motivo = "Quantidade inválida informada para as peças: " + quantidadesInvalidas;
            logger.logError("Dedução de estoque recusada, quantidades inválidas - osId={}, execucaoServicoId={}, pecaIds={}",
                    event.getOsId(), event.getExecucaoServicoId(), quantidadesInvalidas);
            eventPublisher.publish(new DeducaoEstoqueFalhouEvent(event.getOsId(), event.getExecucaoServicoId(), motivo));
            return;
        }

        var deltaPorPecaId = new LinkedHashMap<String, Integer>();
        for (var item : event.getItens()) {
            var alocada = alocadasPorPecaId.get(item.pecaId());
            var delta = item.quantidade() - alocada.getQuantidadeInstalada();
            if (delta > 0) {
                deltaPorPecaId.put(item.pecaId(), delta);
            }
        }

        if (deltaPorPecaId.isEmpty()) {
            logger.logInfo("ServiceEnded já processado anteriormente - execucaoServicoId={}", event.getExecucaoServicoId());
            return;
        }

        try {
            deltaPorPecaId.forEach((pecaId, delta) -> alocadasPorPecaId.get(pecaId).instalar(delta));
            deduzirEstoqueExecucaoUseCase.execute(toDeducaoInput(event, deltaPorPecaId));
            deltaPorPecaId.keySet().forEach(pecaId -> gateway.salvarPecaAlocada(alocadasPorPecaId.get(pecaId)));
            logger.logInfo("ServiceEnded processado com sucesso - execucaoServicoId={}", event.getExecucaoServicoId());
        } catch (IllegalArgumentException | IllegalStateException e) {
            logger.logError("Falha ao deduzir estoque para ServiceEnded - osId=" + event.getOsId()
                    + ", execucaoServicoId=" + event.getExecucaoServicoId(), e);
            eventPublisher.publish(new DeducaoEstoqueFalhouEvent(event.getOsId(), event.getExecucaoServicoId(), e.getMessage()));
        }
    }

    private DeduzirEstoqueExecucaoUseCase.Input toDeducaoInput(ServiceEndedEvent event, Map<String, Integer> deltaPorPecaId) {
        var itens = deltaPorPecaId.entrySet().stream()
                .map(entry -> new DeduzirEstoqueExecucaoUseCase.Input.Item(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
        return new DeduzirEstoqueExecucaoUseCase.Input(event.getExecucaoServicoId(), itens);
    }
}
