package br.com.lata.velha.ordem_servico.infrastructure.config;

import br.com.lata.velha.ordem_servico.application.services.pecaalocada.NotificarAdminEncomendaPecaGateway;
import br.com.lata.velha.ordem_servico.application.services.pecaalocada.NotificarAdminEncomendaPecaService;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.ConferirEstoqueExecucaoUseCase;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.ConferirEstoqueGateway;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque.DeduzirEstoqueExecucaoUseCase;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque.DeduzirEstoqueGateway;
import br.com.lata.velha.shared.application.gateways.EmailProvider;
import br.com.lata.velha.shared.application.gateways.EmailTemplateProvider;
import br.com.lata.velha.shared.application.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Casos de uso que, no modelo de eventos (general-flow.md), são acionados por eventos
 * assíncronos vindos de outros serviços (OrcamentoAprovado, ServiceEnded). Por ora, sem
 * broker, ficam expostos como beans prontos para um futuro listener invocar.
 */
@Configuration
@RequiredArgsConstructor
public class EstoqueExecucaoBeansConfig {

    private final Logger logger;
    private final ConferirEstoqueGateway conferirEstoqueGateway;
    private final DeduzirEstoqueGateway deduzirEstoqueGateway;
    private final NotificarAdminEncomendaPecaGateway notificarAdminGateway;
    private final EmailProvider emailProvider;
    private final EmailTemplateProvider emailTemplateProvider;

    @Bean
    public NotificarAdminEncomendaPecaService notificarAdminEncomendaPecaService() {
        logger.logInfo("Configurando NotificarAdminEncomendaPecaService");
        return new NotificarAdminEncomendaPecaService(notificarAdminGateway, emailProvider, emailTemplateProvider, logger);
    }

    @Bean
    public ConferirEstoqueExecucaoUseCase conferirEstoqueExecucaoUseCase() {
        logger.logInfo("Configurando ConferirEstoqueExecucaoUseCase");
        return new ConferirEstoqueExecucaoUseCase(conferirEstoqueGateway, notificarAdminEncomendaPecaService(), logger);
    }

    @Bean
    public DeduzirEstoqueExecucaoUseCase deduzirEstoqueExecucaoUseCase() {
        logger.logInfo("Configurando DeduzirEstoqueExecucaoUseCase");
        return new DeduzirEstoqueExecucaoUseCase(deduzirEstoqueGateway, logger);
    }
}
