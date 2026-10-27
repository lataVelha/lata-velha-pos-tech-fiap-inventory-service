package br.com.lata.velha.ordem_servico.infrastructure.config;

import br.com.lata.velha.ordem_servico.application.event_handlers.OrcamentoAprovadoEventHandler;
import br.com.lata.velha.ordem_servico.application.event_handlers.ServiceEndedEventHandler;
import br.com.lata.velha.ordem_servico.application.services.pecaalocada.NotificarAdminEncomendaPecaGateway;
import br.com.lata.velha.ordem_servico.application.services.pecaalocada.NotificarAdminEncomendaPecaService;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.ConferirEstoqueExecucaoUseCase;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.ConferirEstoqueGateway;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.ReceberOrcamentoAprovadoGateway;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.ReceberOrcamentoAprovadoUseCase;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque.DeduzirEstoqueExecucaoUseCase;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque.DeduzirEstoqueGateway;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque.ReceberServiceEndedGateway;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque.ReceberServiceEndedUseCase;
import br.com.lata.velha.shared.application.gateways.EmailProvider;
import br.com.lata.velha.shared.application.gateways.EmailTemplateProvider;
import br.com.lata.velha.shared.application.gateways.EventPublisher;
import br.com.lata.velha.shared.application.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class EstoqueExecucaoBeansConfig {

    private final Logger logger;
    private final ConferirEstoqueGateway conferirEstoqueGateway;
    private final DeduzirEstoqueGateway deduzirEstoqueGateway;
    private final NotificarAdminEncomendaPecaGateway notificarAdminGateway;
    private final EmailProvider emailProvider;
    private final EmailTemplateProvider emailTemplateProvider;
    private final ReceberOrcamentoAprovadoGateway receberOrcamentoAprovadoGateway;
    private final ReceberServiceEndedGateway receberServiceEndedGateway;
    private final EventPublisher eventPublisher;

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

    @Bean
    public ReceberOrcamentoAprovadoUseCase receberOrcamentoAprovadoUseCase() {
        logger.logInfo("Configurando ReceberOrcamentoAprovadoUseCase");
        return new ReceberOrcamentoAprovadoUseCase(conferirEstoqueExecucaoUseCase(), receberOrcamentoAprovadoGateway, eventPublisher, logger);
    }

    @Bean
    public ReceberServiceEndedUseCase receberServiceEndedUseCase() {
        logger.logInfo("Configurando ReceberServiceEndedUseCase");
        return new ReceberServiceEndedUseCase(deduzirEstoqueExecucaoUseCase(), receberServiceEndedGateway, eventPublisher, logger);
    }

    @Bean
    public OrcamentoAprovadoEventHandler orcamentoAprovadoEventHandler() {
        return new OrcamentoAprovadoEventHandler(receberOrcamentoAprovadoUseCase());
    }

    @Bean
    public ServiceEndedEventHandler serviceEndedEventHandler() {
        return new ServiceEndedEventHandler(receberServiceEndedUseCase());
    }
}
