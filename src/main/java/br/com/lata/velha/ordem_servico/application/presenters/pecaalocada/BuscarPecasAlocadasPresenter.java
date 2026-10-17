package br.com.lata.velha.ordem_servico.application.presenters.pecaalocada;

import br.com.lata.velha.ordem_servico.application.dtos.response.PecaAlocadaResponse;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;

public interface BuscarPecasAlocadasPresenter {
    PaginatedResult<PecaAlocadaResponse> present(PaginatedResult<PecaAlocada> pecasAlocadas);
}
