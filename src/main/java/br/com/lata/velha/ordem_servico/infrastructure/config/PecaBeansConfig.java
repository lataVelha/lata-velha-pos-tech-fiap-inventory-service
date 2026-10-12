package br.com.lata.velha.ordem_servico.infrastructure.config;

import br.com.lata.velha.ordem_servico.application.controllers.peca.PecaCleanController;
import br.com.lata.velha.ordem_servico.application.controllers.pecaestoque.PecaEstoqueCleanController;
import br.com.lata.velha.ordem_servico.application.presenters.peca.*;
import br.com.lata.velha.ordem_servico.application.presenters.pecaestoque.*;
import br.com.lata.velha.ordem_servico.application.use_cases.peca.*;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque.*;
import br.com.lata.velha.shared.application.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class PecaBeansConfig {

    private final Logger logger;
    private final CadastrarPecaGateway cadastrarGateway;
    private final CadastrarPecaPresenter cadastrarPresenter;
    private final AtualizarPecaGateway atualizarGateway;
    private final AtualizarPecaPresenter atualizarPresenter;
    private final BuscarPecaPorIdGateway buscarPorIdGateway;
    private final BuscarPecaPorIdPresenter buscarPorIdPresenter;
    private final BuscarPecasGateway buscarTodosGateway;
    private final BuscarPecasPresenter buscarTodosPresenter;
    private final DesativarPecaGateway desativarGateway;

    private final BuscarPecaEstoqueGateway buscarEstoqueGateway;
    private final BuscarPecaEstoquePresenter buscarEstoquePresenter;
    private final EntradaPecaEstoqueGateway entradaGateway;
    private final EntradaPecaEstoquePresenter entradaPresenter;
    private final SaidaPecaEstoqueGateway saidaGateway;
    private final SaidaPecaEstoquePresenter saidaPresenter;
    private final AjustarPecaEstoqueGateway ajustarGateway;
    private final AjustarPecaEstoquePresenter ajustarPresenter;

    @Bean
    public PecaCleanController pecaCleanController() {
        logger.logInfo("Configurando PecaCleanController");
        return new PecaCleanController(
                cadastrarGateway,
                cadastrarPresenter,
                atualizarGateway,
                atualizarPresenter,
                buscarPorIdGateway,
                buscarPorIdPresenter,
                buscarTodosGateway,
                buscarTodosPresenter,
                desativarGateway,
                logger);
    }

    @Bean
    public PecaEstoqueCleanController pecaEstoqueCleanController() {
        logger.logInfo("Configurando PecaEstoqueCleanController");
        return new PecaEstoqueCleanController(
                buscarEstoqueGateway,
                buscarEstoquePresenter,
                entradaGateway,
                entradaPresenter,
                saidaGateway,
                saidaPresenter,
                ajustarGateway,
                ajustarPresenter,
                logger);
    }
}
