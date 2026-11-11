package br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories;

import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface PecaMongoRepository extends MongoRepository<PecaEntity, String> {

    Optional<PecaEntity> findByIdAndAtivoTrue(String id);

    Page<PecaEntity> findByAtivoTrue(Pageable pageable);

    boolean existsByIdAndAtivoTrue(String pecaId);

    List<PecaEntity> findAllByIdIn(Collection<String> ids);

    Set<PecaEntity> getAllByIdInAndAtivoTrue(Set<String> pecasIds);
}
