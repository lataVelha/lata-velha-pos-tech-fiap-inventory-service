package br.com.lata.velha.ordem_servico.application.use_cases.peca;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;

public interface BuscarPecasGateway {
    PaginatedResult<Peca> findAll(int page, int size);
}
