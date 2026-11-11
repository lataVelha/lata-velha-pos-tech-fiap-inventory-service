package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;

import java.util.List;

public interface EntradaPecaEstoqueGateway {
    Peca getPecaAtivaPorId(String pecaId);
    PecaEstoque getEstoquePorPecaId(String pecaId);
    PecaEstoque salvarEstoque(PecaEstoque estoque);
    List<PecaAlocada> buscarPendentesPorPeca(String pecaId);
    PecaAlocada salvarPecaAlocada(PecaAlocada pecaAlocada);
    List<PecaAlocada> getTodasPecaAlocadaPorExecucaoServicoId(Long execucaoServicoId);
}
