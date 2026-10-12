package br.com.lata.velha.ordem_servico.application.use_cases.peca;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;

public interface CadastrarPecaGateway {
    Peca salvarPeca(Peca peca);
    PecaEstoque salvarEstoque(PecaEstoque estoque);
}
