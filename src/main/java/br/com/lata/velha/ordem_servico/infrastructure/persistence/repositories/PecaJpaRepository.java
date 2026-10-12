package br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories;

import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PecaJpaRepository extends JpaRepository<PecaEntity, Long> {

    Optional<PecaEntity> findByIdAndAtivoTrue(Long id);

    Page<PecaEntity> findByAtivoTrue(Pageable pageable);

    boolean existsByIdAndAtivoTrue(Long pecaId);

    List<PecaEntity> findAllByIdIn(Collection<Long> ids);

    Set<PecaEntity> getAllByIdInAndAtivoTrue(Set<Long> pecasIds);
}
