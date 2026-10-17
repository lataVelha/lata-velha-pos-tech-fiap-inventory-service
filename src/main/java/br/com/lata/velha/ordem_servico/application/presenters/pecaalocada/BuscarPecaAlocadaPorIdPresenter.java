package br.com.lata.velha.ordem_servico.application.presenters.pecaalocada;

import br.com.lata.velha.ordem_servico.application.dtos.response.PecaAlocadaResponse;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;

public interface BuscarPecaAlocadaPorIdPresenter {
    PecaAlocadaResponse present(PecaAlocada pecaAlocada);
}
