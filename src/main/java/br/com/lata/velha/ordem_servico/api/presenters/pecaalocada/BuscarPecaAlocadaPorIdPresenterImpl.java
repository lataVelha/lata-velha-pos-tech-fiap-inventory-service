package br.com.lata.velha.ordem_servico.api.presenters.pecaalocada;

import br.com.lata.velha.ordem_servico.application.dtos.response.PecaAlocadaResponse;
import br.com.lata.velha.ordem_servico.application.presenters.pecaalocada.BuscarPecaAlocadaPorIdPresenter;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import org.springframework.stereotype.Component;

@Component
public class BuscarPecaAlocadaPorIdPresenterImpl implements BuscarPecaAlocadaPorIdPresenter {
    @Override
    public PecaAlocadaResponse present(PecaAlocada pecaAlocada) {
        return PecaAlocadaResponse.from(pecaAlocada);
    }
}
