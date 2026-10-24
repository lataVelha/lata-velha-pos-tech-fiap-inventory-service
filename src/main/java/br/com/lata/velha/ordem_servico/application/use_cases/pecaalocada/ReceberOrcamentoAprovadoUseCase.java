package br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import br.com.lata.velha.ordem_servico.domain.events.InventoryCheckedEvent;
import br.com.lata.velha.ordem_servico.domain.events.OrcamentoAprovadoEvent;
import br.com.lata.velha.shared.application.gateways.EventPublisher;
import br.com.lata.velha.shared.application.logging.Logger;

import java.util.LinkedHashSet;
import java.util.stream.Collectors;

public class ReceberOrcamentoAprovadoUseCase {

    private final ConferirEstoqueExecucaoUseCase conferirEstoqueExecucaoUseCase;
    private final ReceberOrcamentoAprovadoGateway gateway;
    private final EventPublisher eventPublisher;
    private final Logger logger;

    public ReceberOrcamentoAprovadoUseCase(ConferirEstoqueExecucaoUseCase conferirEstoqueExecucaoUseCase,
                                           ReceberOrcamentoAprovadoGateway gateway,
                                           EventPublisher eventPublisher,
                                           Logger logger) {
        this.conferirEstoqueExecucaoUseCase = conferirEstoqueExecucaoUseCase;
        this.gateway = gateway;
        this.eventPublisher = eventPublisher;
        this.logger = logger;
    }

    public void execute(OrcamentoAprovadoEvent event) {
        logger.logInfo("Processando OrcamentoAprovado - osId={}, quantidadeItens={}", event.getOsId(), event.getItens().size());

        var itensNovos = event.getItens().stream()
                .filter(item -> gateway.buscarPecaAlocadaExistente(item.execucaoServicoId(), item.pecaId()).isEmpty())
                .toList();

        if (!itensNovos.isEmpty()) {
            var pecaIds = itensNovos.stream().map(OrcamentoAprovadoEvent.Item::pecaId).collect(Collectors.toSet());
            var precoPorPecaId = gateway.getPecasPorIds(pecaIds).stream()
                    .collect(Collectors.toMap(Peca::getId, Peca::getValor));

            var conferirInput = new ConferirEstoqueExecucaoUseCase.Input(itensNovos.stream()
                    .map(item -> new ConferirEstoqueExecucaoUseCase.Input.Item(
                            item.execucaoServicoId(), item.pecaId(), item.quantidade(), precoPorPecaId.get(item.pecaId())))
                    .toList());
            conferirEstoqueExecucaoUseCase.execute(conferirInput);
        }

        var execucaoServicoIds = event.getItens().stream()
                .map(OrcamentoAprovadoEvent.Item::execucaoServicoId)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        var itensDisponibilidade = execucaoServicoIds.stream()
                .map(this::toItemDisponibilidade)
                .toList();

        eventPublisher.publish(new InventoryCheckedEvent(event.getOsId(), itensDisponibilidade));
        logger.logInfo("OrcamentoAprovado processado, InventoryChecked publicado - osId={}", event.getOsId());
    }

    private InventoryCheckedEvent.ItemDisponibilidade toItemDisponibilidade(Long execucaoServicoId) {
        var todasDaExecucao = gateway.getTodasPecaAlocadaPorExecucaoServicoId(execucaoServicoId);
        var pecasDisponiveis = todasDaExecucao.stream().allMatch(this::isSatisfeita);
        return new InventoryCheckedEvent.ItemDisponibilidade(execucaoServicoId, pecasDisponiveis);
    }

    private boolean isSatisfeita(PecaAlocada pecaAlocada) {
        return pecaAlocada.getStatus() == StatusPecaAlocada.RESERVADA || pecaAlocada.getStatus() == StatusPecaAlocada.INSTALADA;
    }
}
