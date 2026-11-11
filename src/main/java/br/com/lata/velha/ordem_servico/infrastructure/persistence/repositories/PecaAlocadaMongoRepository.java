package br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories;

import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaAlocadaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PecaAlocadaMongoRepository extends MongoRepository<PecaAlocadaEntity, String> {

    Page<PecaAlocadaEntity> findByExecucaoServicoId(Long servicoOsId, Pageable pageable);

    List<PecaAlocadaEntity> findAllByExecucaoServicoId(Long servicoOsId);

    @Aggregation(pipeline = {
            "{ $match: { 'pecaId': ?0, 'status': { $in: ['RESERVADA', 'PARCIAL'] } } }",
            "{ $group: { _id: null, total: { $sum: '$quantidadeReservada' } } }"
    })
    Integer somarQuantidadeReservadaPorPeca(String pecaId);

    @Query(value = "{ 'pecaId': ?0, 'status': { $in: ['PARCIAL', 'ENCOMENDA'] } }", sort = "{ 'atualizado': 1 }")
    List<PecaAlocadaEntity> buscarPendentesPorPecaOrdenado(String pecaId);

    Optional<PecaAlocadaEntity> findByPecaIdAndExecucaoServicoId(String pecaId, Long servicoId);
}

