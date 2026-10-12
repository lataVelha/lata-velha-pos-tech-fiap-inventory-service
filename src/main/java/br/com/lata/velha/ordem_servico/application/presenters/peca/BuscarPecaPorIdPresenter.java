package br.com.lata.velha.ordem_servico.application.presenters.peca;

import br.com.lata.velha.ordem_servico.application.dtos.response.PecaResponse;
import br.com.lata.velha.ordem_servico.domain.entities.Peca;

public interface BuscarPecaPorIdPresenter {
    PecaResponse present(Peca peca);
}
