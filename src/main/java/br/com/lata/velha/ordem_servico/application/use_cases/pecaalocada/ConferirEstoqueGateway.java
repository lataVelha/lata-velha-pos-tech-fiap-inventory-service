package br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface ConferirEstoqueGateway {
    PecaAlocada getOuCriarPecaAlocada(Long execucaoServicoId, String pecaId, Integer quantidadeSolicitada, BigDecimal valorUnitarioPeca);
    List<PecaEstoque> getEstoquePorPecaIds(Set<String> pecaIds);
    void salvarEstoques(Collection<PecaEstoque> estoques);
    PecaAlocada salvarPecaAlocada(PecaAlocada pecaAlocada);
}
