package br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories;

import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.ordem_servico.domain.repositories.PecaEstoqueRepository;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEstoqueEntity;
import br.com.lata.velha.shared.application.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class PecaEstoqueRepositoryImpl implements PecaEstoqueRepository {

    private final PecaEstoqueMongoRepository jpaRepository;
    private final Logger logger;

    @Override
    public PecaEstoque save(PecaEstoque pecaEstoque) {
        var entity = PecaEstoqueEntity.fromDomain(pecaEstoque);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Collection<PecaEstoque> saveAll(Collection<PecaEstoque> collection) {
        var entities = collection.stream().map(PecaEstoqueEntity::fromDomain).toList();
        var saved = jpaRepository.saveAll(entities);
        return saved.stream().map(PecaEstoqueEntity::toDomain).toList();
    }

    @Override
    public Optional<PecaEstoque> findByPecaId(String pecaId) {
        return jpaRepository.findById(pecaId)
                .map(PecaEstoqueEntity::toDomain);
    }

    @Override
    public void baixarEstoque(String pecaId, Integer quantidade) {
        jpaRepository.baixarEstoque(pecaId, quantidade);
    }

    @Override
    public List<PecaEstoque> findAllByPecaIds(Set<String> pecaIds) {
        return jpaRepository.findAllByPecaIdIn(pecaIds).stream()
                .map(PecaEstoqueEntity::toDomain)
                .toList();
    }
}
