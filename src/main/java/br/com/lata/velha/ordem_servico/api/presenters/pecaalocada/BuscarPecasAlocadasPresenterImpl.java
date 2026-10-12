package br.com.lata.velha.ordem_servico.api.presenters.pecaalocada;

import br.com.lata.velha.ordem_servico.application.dtos.response.PecaAlocadaResponse;
import br.com.lata.velha.ordem_servico.application.presenters.pecaalocada.BuscarPecasAlocadasPresenter;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
import org.springframework.stereotype.Component;

@Component
public class BuscarPecasAlocadasPresenterImpl implements BuscarPecasAlocadasPresenter {
    @Override
    public PaginatedResult<PecaAlocadaResponse> present(PaginatedResult<PecaAlocada> pecasAlocadas) {
        return PaginatedResult.map(pecasAlocadas, PecaAlocadaResponse::from);
    }
}
