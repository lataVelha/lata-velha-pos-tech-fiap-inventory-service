package br.com.lata.velha.ordem_servico.api.presenters.pecaestoque;

import br.com.lata.velha.ordem_servico.application.dtos.response.PecaEstoqueResponse;
import br.com.lata.velha.ordem_servico.application.presenters.pecaestoque.BuscarPecaEstoquePresenter;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import org.springframework.stereotype.Component;

@Component
public class BuscarPecaEstoquePresenterImpl implements BuscarPecaEstoquePresenter {
    @Override
    public PecaEstoqueResponse present(PecaEstoque estoque) {
        return PecaEstoqueResponse.from(estoque);
    }
}
