package br.com.lata.velha.ordem_servico.domain.repositories;

import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;

import java.util.List;

public interface PecaAlocadaRepository {

    PecaAlocada save(PecaAlocada pecaAlocada);

    List<PecaAlocada> saveAll(List<PecaAlocada> pecasAlocadas);

    PecaAlocada findById(Long id);

    PaginatedResult<PecaAlocada> findByServicoOsId(Long servicoOsId, int page, int size);

    void delete(Long id);

    List<PecaAlocada> buscarPendentesPorPecaOrdenado(Long pecaId);

    PecaAlocada findByPecaIdAndServicoOsId(Long pecaId, Long servicoOsId);
}