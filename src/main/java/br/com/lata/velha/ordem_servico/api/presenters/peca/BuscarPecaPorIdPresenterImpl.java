package br.com.lata.velha.ordem_servico.api.presenters.peca;

import br.com.lata.velha.ordem_servico.application.dtos.response.PecaResponse;
import br.com.lata.velha.ordem_servico.application.presenters.peca.BuscarPecaPorIdPresenter;
import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import org.springframework.stereotype.Component;

@Component
public class BuscarPecaPorIdPresenterImpl implements BuscarPecaPorIdPresenter {
    @Override
    public PecaResponse present(Peca peca) {
        return PecaResponse.from(peca);
    }
}
