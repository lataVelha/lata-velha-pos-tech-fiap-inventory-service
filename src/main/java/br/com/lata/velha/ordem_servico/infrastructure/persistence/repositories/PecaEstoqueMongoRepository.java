package br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories;

import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEstoqueEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Update;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface PecaEstoqueMongoRepository extends MongoRepository<PecaEstoqueEntity, String> {

    @Query("{ 'pecaId' : ?0 }")
    @Update("{ '$inc' : { 'quantidadeArmazenada' : -?1 } }")
    void baixarEstoque(String pecaId, Integer quantidade);

    List<PecaEstoqueEntity> findAllByPecaIdIn(Set<String> pecaIds);
}
