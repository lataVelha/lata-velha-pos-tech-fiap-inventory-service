package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.shared.application.logging.Logger;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Deduz do estoque as peças utilizadas ao finalizar uma execução de serviço — lógica
 * trazida de FinalizarServicoUseCase.retirarEstoques do OsService.
 */
public class DeduzirEstoqueExecucaoUseCase {

    private final DeduzirEstoqueGateway gateway;
    private final Logger logger;

    public DeduzirEstoqueExecucaoUseCase(DeduzirEstoqueGateway gateway, Logger logger) {
        this.gateway = gateway;
        this.logger = logger;
    }

    public void execute(Input input) {
        logger.logInfo("Deduzindo peças do estoque - execucaoServicoId={}, quantidadeItens={}",
                input.execucaoServicoId(), input.itens().size());
        var quantidadesMap = input.itens().stream()
                .collect(Collectors.toMap(Input.Item::pecaId, Input.Item::quantidade));
        var estoques = gateway.getEstoquePorPecaIds(quantidadesMap.keySet());
        if (estoques.size() != quantidadesMap.size()) {
            var idsEncontrados = estoques.stream().map(PecaEstoque::getPecaId).collect(Collectors.toSet());
            var idsInvalidos = new HashSet<>(quantidadesMap.keySet());
            idsInvalidos.removeAll(idsEncontrados);
            logger.logWarn("Dedução de estoque rejeitada: peças sem registro de estoque - pecaIds={}", idsInvalidos);
            throw new IllegalStateException("Nem todas as peças possuem registro de estoque");
        }
        estoques.forEach(estoque -> estoque.retirar(quantidadesMap.get(estoque.getPecaId())));
        gateway.salvarEstoques(estoques);
        logger.logInfo("Dedução de estoque concluída - execucaoServicoId={}", input.execucaoServicoId());
    }

    public record Input(Long execucaoServicoId, List<Item> itens) {
        public record Item(Long pecaId, Integer quantidade) {}
    }
}
