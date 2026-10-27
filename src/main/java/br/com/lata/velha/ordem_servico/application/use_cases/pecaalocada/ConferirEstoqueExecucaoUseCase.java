package br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada;

import br.com.lata.velha.ordem_servico.application.services.pecaalocada.NotificarAdminEncomendaPecaService;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.shared.application.logging.Logger;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Confere e reserva estoque para as peças necessárias de execuções de serviço — lógica
 * trazida de AprovarOrdemServicoUseCase/ReceberAprovacaoOrcamentoClienteUseCase do OsService.
 */
public class ConferirEstoqueExecucaoUseCase {

    private final ConferirEstoqueGateway gateway;
    private final NotificarAdminEncomendaPecaService notificarAdminEncomendaService;
    private final Logger logger;

    public ConferirEstoqueExecucaoUseCase(ConferirEstoqueGateway gateway,
                                           NotificarAdminEncomendaPecaService notificarAdminEncomendaService,
                                           Logger logger) {
        this.gateway = gateway;
        this.notificarAdminEncomendaService = notificarAdminEncomendaService;
        this.logger = logger;
    }

    public List<PecaAlocada> execute(Input input) {
        logger.logInfo("Conferindo estoque - quantidadeItens={}", input.itens().size());
        var pecaIds = input.itens().stream().map(Input.Item::pecaId).collect(Collectors.toSet());
        var estoqueMap = gateway.getEstoquePorPecaIds(pecaIds).stream()
                .collect(Collectors.toMap(PecaEstoque::getPecaId, e -> e));

        var idsInvalidos = new HashSet<>(pecaIds);
        idsInvalidos.removeAll(estoqueMap.keySet());
        if (!idsInvalidos.isEmpty()) {
            logger.logWarn("Conferência de estoque rejeitada: peças sem registro de estoque - pecaIds={}", idsInvalidos);
            throw new IllegalArgumentException("Peças sem registro de estoque: " + idsInvalidos);
        }

        var resultado = input.itens().stream().map(item -> {
            var pecaAlocada = gateway.getOuCriarPecaAlocada(item.execucaoServicoId(), item.pecaId(), item.quantidadeSolicitada(), item.valorUnitarioPeca());
            var estoque = estoqueMap.get(item.pecaId());
            pecaAlocada.reservar(estoque);

            if (pecaAlocada.getQuantidadeEncomendada() != null && pecaAlocada.getQuantidadeEncomendada() > 0) {
                logger.logInfo("Peça com estoque insuficiente, encomendando - pecaId={}, quantidadeEncomendada={}",
                        item.pecaId(), pecaAlocada.getQuantidadeEncomendada());
                notificarAdminEncomendaService.execute(new NotificarAdminEncomendaPecaService.Input(
                        item.execucaoServicoId(), item.pecaId(), pecaAlocada.getQuantidadeEncomendada()));
            }
            return gateway.salvarPecaAlocada(pecaAlocada);
        }).toList();

        gateway.salvarEstoques(estoqueMap.values());
        logger.logInfo("Conferência de estoque concluída - quantidadeItens={}", resultado.size());
        return resultado;
    }

    public record Input(List<Item> itens) {
        public record Item(Long execucaoServicoId, Long pecaId, Integer quantidadeSolicitada, BigDecimal valorUnitarioPeca) {}
    }
}
