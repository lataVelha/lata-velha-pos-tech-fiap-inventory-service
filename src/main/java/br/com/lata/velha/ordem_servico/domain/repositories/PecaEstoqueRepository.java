package br.com.lata.velha.ordem_servico.domain.repositories;

import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PecaEstoqueRepository {

    PecaEstoque save(PecaEstoque pecaEstoque);

    Collection<PecaEstoque> saveAll(Collection<PecaEstoque> collection);

    Optional<PecaEstoque> findByPecaId(String pecaId);

    void baixarEstoque(String pecaId, Integer quantidade);

    List<PecaEstoque> findAllByPecaIds(Set<String> pecaIds);
}
