package br.com.lata.velha.ordem_servico.application.presenters.peca;

import br.com.lata.velha.ordem_servico.application.dtos.response.PecaResponse;
import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;

public interface BuscarPecasPresenter {
    PaginatedResult<PecaResponse> present(PaginatedResult<Peca> pecas);
}
