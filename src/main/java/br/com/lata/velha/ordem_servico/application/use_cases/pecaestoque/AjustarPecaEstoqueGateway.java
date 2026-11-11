package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;

public interface AjustarPecaEstoqueGateway {
    Peca getPecaAtivaPorId(String pecaId);
    PecaEstoque getEstoquePorPecaId(String pecaId);
    PecaEstoque salvarEstoque(PecaEstoque estoque);
}
