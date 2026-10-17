package br.com.lata.velha.ordem_servico.application.presenters.pecaestoque;

import br.com.lata.velha.ordem_servico.application.dtos.response.PecaEstoqueResponse;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;

public interface EntradaPecaEstoquePresenter {
    PecaEstoqueResponse present(PecaEstoque estoque);
}
