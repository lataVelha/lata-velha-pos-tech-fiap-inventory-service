package br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories;

import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaAlocadaEntity;
import br.com.lata.velha.shared.application.logging.Logger;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.repositories.PecaAlocadaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class PecaAlocadaRepositoryImpl implements PecaAlocadaRepository {
    private final PecaAlocadaJpaRepository jpaRepository;
    private final Logger logger;

    @Override
    public PecaAlocada save(PecaAlocada pecaAlocada) {
        var entity = PecaAlocadaEntity.fromDomain(pecaAlocada);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public List<PecaAlocada> saveAll(List<PecaAlocada> pecasAlocadas) {
        var entities = pecasAlocadas.stream()
                .map(PecaAlocadaEntity::fromDomain)
                .toList();
        var saved = jpaRepository.saveAll(entities);
        return saved.stream()
                .map(PecaAlocadaEntity::toDomain)
                .toList();
    }

    @Override
    public PecaAlocada findById(Long id) {
        return jpaRepository.findById(id)
                .map(PecaAlocadaEntity::toDomain)
                .orElseThrow(() -> {
                    logger.logWarn("Peça alocada não encontrada - pecaAlocadaId={}", id);
                    return new IllegalArgumentException("Peça alocada não encontrada");
                });
    }

    @Override
    public PaginatedResult<PecaAlocada> findByServicoOsId(Long servicoOsId, int page, int size) {
        var result = jpaRepository.findByExecucaoServicoId(servicoOsId, PageRequest.of(page, size));
        var content = result.getContent().stream().map(PecaAlocadaEntity::toDomain).toList();

        return new PaginatedResult<>(
                content,
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    public void delete(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public List<PecaAlocada> buscarPendentesPorPecaOrdenado(Long pecaId) {
        return jpaRepository.buscarPendentesPorPecaOrdenado(pecaId)
                .stream().map(PecaAlocadaEntity::toDomain).toList();
    }

    @Override
    public PecaAlocada findByPecaIdAndServicoOsId(Long pecaId, Long servicoOsId) {
        return  jpaRepository.findByPecaIdAndExecucaoServicoId(pecaId,servicoOsId)
                .map(PecaAlocadaEntity::toDomain)
                .orElseThrow(() -> {
                    logger.logWarn("Peça alocada não encontrada - pecaId={}, servicoOsId={}", pecaId, servicoOsId);
                    return new IllegalArgumentException("Peça alocada não encontrada");
                });
    }
}