package br.com.lata.velha.ordem_servico.domain.repositories;

import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;

import java.util.List;

public interface PecaAlocadaRepository {

    PecaAlocada save(PecaAlocada pecaAlocada);

    List<PecaAlocada> saveAll(List<PecaAlocada> pecasAlocadas);

    PecaAlocada findById(String id);

    PaginatedResult<PecaAlocada> findByServicoOsId(Long servicoOsId, int page, int size);

    void delete(String id);

    List<PecaAlocada> buscarPendentesPorPecaOrdenado(String pecaId);

    PecaAlocada findByPecaIdAndServicoOsId(String pecaId, Long servicoOsId);

    List<PecaAlocada> findAllByExecucaoServicoId(Long execucaoServicoId);
}