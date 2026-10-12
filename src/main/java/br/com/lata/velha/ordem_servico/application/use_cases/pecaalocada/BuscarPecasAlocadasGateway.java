package br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;

public interface BuscarPecasAlocadasGateway {
    PaginatedResult<PecaAlocada> findByExecucaoServicoId(Long execucaoServicoId, int page, int size);
}
