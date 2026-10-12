package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public interface DeduzirEstoqueGateway {
    List<PecaEstoque> getEstoquePorPecaIds(Set<Long> pecaIds);
    void salvarEstoques(Collection<PecaEstoque> estoques);
}
