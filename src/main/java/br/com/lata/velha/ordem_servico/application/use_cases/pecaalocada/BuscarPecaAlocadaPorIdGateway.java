package br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;

public interface BuscarPecaAlocadaPorIdGateway {
    PecaAlocada getPecaAlocadaPorId(Long id);
}
