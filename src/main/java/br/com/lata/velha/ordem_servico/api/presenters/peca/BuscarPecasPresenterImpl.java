package br.com.lata.velha.ordem_servico.api.presenters.peca;

import br.com.lata.velha.ordem_servico.application.dtos.response.PecaResponse;
import br.com.lata.velha.ordem_servico.application.presenters.peca.BuscarPecasPresenter;
import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
import org.springframework.stereotype.Component;

@Component
public class BuscarPecasPresenterImpl implements BuscarPecasPresenter {
    @Override
    public PaginatedResult<PecaResponse> present(PaginatedResult<Peca> pecas) {
        return PaginatedResult.map(pecas, PecaResponse::from);
    }
}
