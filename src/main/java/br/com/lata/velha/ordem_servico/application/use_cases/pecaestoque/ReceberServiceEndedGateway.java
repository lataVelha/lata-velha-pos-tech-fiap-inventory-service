package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;

import java.util.Optional;

public interface ReceberServiceEndedGateway {
    Optional<PecaAlocada> buscarPecaAlocadaExistente(Long execucaoServicoId, Long pecaId);
    PecaAlocada salvarPecaAlocada(PecaAlocada pecaAlocada);
}
