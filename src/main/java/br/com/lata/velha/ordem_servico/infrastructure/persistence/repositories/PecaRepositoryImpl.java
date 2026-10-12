package br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.domain.repositories.PecaRepository;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEntity;
import br.com.lata.velha.shared.application.logging.Logger;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PecaRepositoryImpl implements PecaRepository {
    private final PecaJpaRepository jpaRepository;
    private final Logger logger;

    @Override
    public Peca save(Peca peca) {
        var entity = PecaEntity.fromDomain(peca);
        var saved = jpaRepository.save(entity);
        return saved.toDomain();
    }

    @Override
    public Peca getActiveById(Long id) {
        return jpaRepository.findByIdAndAtivoTrue(id)
                .map(PecaEntity::toDomain)
                .orElseThrow(() -> {
                    logger.logWarn("Peça ativa não encontrada - pecaId={}", id);
                    return new IllegalArgumentException("Peça não encontrada");
                });
    }

    @Override
    public PaginatedResult<Peca> findAllActivePaginated(int page, int size) {
        var result = jpaRepository.findByAtivoTrue(PageRequest.of(page, size));
        var content = result.getContent().stream().map(PecaEntity::toDomain).toList();

        return new PaginatedResult<>(
                content,
                page,
                size,
                result.getTotalElements(),
                result.getTotalPages()
        );
    }

    @Override
    public boolean existsActiveById(Long pecaId) {
        return jpaRepository.existsByIdAndAtivoTrue(pecaId);
    }

    @Override
    public Set<Peca> getAllActiveByIds(Set<Long> pecasIds) {
        return jpaRepository.getAllByIdInAndAtivoTrue(pecasIds)
                .stream()
                .map(PecaEntity::toDomain)
                .collect(Collectors.toSet());
    }

    @Override
    public List<Peca> findAllByIds(Set<Long> ids) {
        return jpaRepository.findAllByIdIn(ids).stream()
                .map(PecaEntity::toDomain)
                .toList();
    }
}
