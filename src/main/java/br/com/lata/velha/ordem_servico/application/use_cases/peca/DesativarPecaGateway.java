package br.com.lata.velha.ordem_servico.application.use_cases.peca;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;

public interface DesativarPecaGateway {
    Peca getPecaAtivaPorId(Long id);
    Peca salvarPeca(Peca peca);
}
