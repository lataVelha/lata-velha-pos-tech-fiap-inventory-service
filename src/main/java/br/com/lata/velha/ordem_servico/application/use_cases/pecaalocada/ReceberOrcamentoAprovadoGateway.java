package br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReceberOrcamentoAprovadoGateway {
    Optional<PecaAlocada> buscarPecaAlocadaExistente(Long execucaoServicoId, Long pecaId);
    List<Peca> getPecasPorIds(Set<Long> ids);
    List<PecaAlocada> getTodasPecaAlocadaPorExecucaoServicoId(Long execucaoServicoId);
}
