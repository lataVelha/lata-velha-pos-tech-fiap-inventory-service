package br.com.lata.velha.ordem_servico.domain.repositories;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;

import java.util.List;
import java.util.Set;

public interface PecaRepository {

    Peca save(Peca peca);

    Peca getActiveById(String id);

    List<Peca> findAllByIds(Set<String> ids);

    PaginatedResult<Peca> findAllActivePaginated(int page, int size);

    boolean existsActiveById(String pecaId);

    Set<Peca> getAllActiveByIds(Set<String> pecasIds);
}
