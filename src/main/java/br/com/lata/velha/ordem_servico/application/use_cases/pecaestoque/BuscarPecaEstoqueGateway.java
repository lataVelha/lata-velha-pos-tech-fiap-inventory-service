package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;

public interface BuscarPecaEstoqueGateway {
    Peca getPecaAtivaPorId(Long pecaId);
    PecaEstoque getEstoquePorPecaId(Long pecaId);
}
