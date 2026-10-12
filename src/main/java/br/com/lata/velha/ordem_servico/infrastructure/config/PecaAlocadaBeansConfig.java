package br.com.lata.velha.ordem_servico.infrastructure.config;

import br.com.lata.velha.ordem_servico.application.controllers.pecaalocada.PecaAlocadaCleanController;
import br.com.lata.velha.ordem_servico.application.presenters.pecaalocada.BuscarPecaAlocadaPorIdPresenter;
import br.com.lata.velha.ordem_servico.application.presenters.pecaalocada.BuscarPecasAlocadasPresenter;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.BuscarPecaAlocadaPorIdGateway;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.BuscarPecasAlocadasGateway;
import br.com.lata.velha.shared.application.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PecaAlocadaBeansConfig {

    private final Logger logger;
    private final BuscarPecaAlocadaPorIdGateway buscarPorIdGateway;
    private final BuscarPecaAlocadaPorIdPresenter buscarPorIdPresenter;
    private final BuscarPecasAlocadasGateway buscarTodosGateway;
    private final BuscarPecasAlocadasPresenter buscarTodosPresenter;

    @Bean
    public PecaAlocadaCleanController pecaAlocadaCleanController() {
        logger.logInfo("Configurando PecaAlocadaCleanController");
        return new PecaAlocadaCleanController(
                buscarPorIdGateway,
                buscarPorIdPresenter,
                buscarTodosGateway,
                buscarTodosPresenter,
                logger);
    }
}
