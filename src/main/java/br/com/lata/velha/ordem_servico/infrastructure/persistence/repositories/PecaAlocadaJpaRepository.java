package br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories;

import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaAlocadaEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PecaAlocadaJpaRepository extends JpaRepository<PecaAlocadaEntity, Long> {
    Page<PecaAlocadaEntity> findByExecucaoServicoId(Long servicoOsId, Pageable pageable);

    @Query("""
                select coalesce(sum(p.quantidadeReservada),0)
                from PecaAlocadaEntity p
                where p.pecaId = :pecaId
                  and p.status in ('RESERVADA','PARCIAL')
            """)
    Integer somarQuantidadeReservadaPorPeca(@Param("pecaId") Long pecaId);

    @Query("""
                select p
                from PecaAlocadaEntity p
                where p.pecaId = :pecaId
                  and p.status in ('PARCIAL','ENCOMENDA')
                order by p.atualizado asc
            """)
    List<PecaAlocadaEntity> buscarPendentesPorPecaOrdenado(@Param("pecaId") Long pecaId);

    Optional<PecaAlocadaEntity> findByPecaIdAndExecucaoServicoId(Long pecaId, Long servicoId);
}
