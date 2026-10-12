package br.com.lata.velha.ordem_servico.domain.repositories;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;

import java.util.List;
import java.util.Set;

public interface PecaRepository {

    Peca save(Peca peca);

    Peca getActiveById(Long id);

    List<Peca> findAllByIds(Set<Long> ids);

    PaginatedResult<Peca> findAllActivePaginated(int page, int size);

    boolean existsActiveById(Long pecaId);

    Set<Peca> getAllActiveByIds(Set<Long> pecasIds);
}
