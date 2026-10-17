package br.com.lata.velha.ordem_servico.application.controllers.peca;

import br.com.lata.velha.ordem_servico.application.dtos.request.AtualizarPecaRequest;
import br.com.lata.velha.ordem_servico.application.dtos.request.CadastrarPecaRequest;
import br.com.lata.velha.ordem_servico.application.dtos.response.PecaResponse;
import br.com.lata.velha.ordem_servico.application.presenters.peca.*;
import br.com.lata.velha.ordem_servico.application.use_cases.peca.*;
import br.com.lata.velha.shared.application.logging.Logger;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;

public class PecaCleanController {

    private final CadastrarPecaGateway cadastrarGateway;
    private final CadastrarPecaPresenter cadastrarPresenter;

    private final AtualizarPecaGateway atualizarGateway;
    private final AtualizarPecaPresenter atualizarPresenter;

    private final BuscarPecaPorIdGateway buscarPorIdGateway;
    private final BuscarPecaPorIdPresenter buscarPorIdPresenter;

    private final BuscarPecasGateway buscarTodosGateway;
    private final BuscarPecasPresenter buscarTodosPresenter;

    private final DesativarPecaGateway desativarGateway;

    private final Logger logger;

    public PecaCleanController(CadastrarPecaGateway cadastrarGateway,
                               CadastrarPecaPresenter cadastrarPresenter,
                               AtualizarPecaGateway atualizarGateway,
                               AtualizarPecaPresenter atualizarPresenter,
                               BuscarPecaPorIdGateway buscarPorIdGateway,
                               BuscarPecaPorIdPresenter buscarPorIdPresenter,
                               BuscarPecasGateway buscarTodosGateway,
                               BuscarPecasPresenter buscarTodosPresenter,
                               DesativarPecaGateway desativarGateway,
                               Logger logger) {
        this.cadastrarGateway = cadastrarGateway;
        this.cadastrarPresenter = cadastrarPresenter;
        this.atualizarGateway = atualizarGateway;
        this.atualizarPresenter = atualizarPresenter;
        this.buscarPorIdGateway = buscarPorIdGateway;
        this.buscarPorIdPresenter = buscarPorIdPresenter;
        this.buscarTodosGateway = buscarTodosGateway;
        this.buscarTodosPresenter = buscarTodosPresenter;
        this.desativarGateway = desativarGateway;
        this.logger = logger;
    }

    public PecaResponse cadastrar(CadastrarPecaRequest request) {
        logger.logInfo("Iniciando cadastro de peça");
        var peca = new CadastrarPecaUseCase(cadastrarGateway, logger).execute(request);
        logger.logInfo("Cadastro de peça concluído com sucesso - pecaId={}", peca.getId());
        return cadastrarPresenter.present(peca);
    }

    public PecaResponse atualizar(Long id, AtualizarPecaRequest request) {
        logger.logInfo("Iniciando atualização de peça - pecaId={}", id);
        var peca = new AtualizarPecaUseCase(atualizarGateway, logger).execute(id, request);
        logger.logInfo("Atualização de peça concluída com sucesso - pecaId={}", id);
        return atualizarPresenter.present(peca);
    }

    public PecaResponse buscarPorId(Long id) {
        logger.logInfo("Iniciando busca de peça por id - pecaId={}", id);
        var peca = new BuscarPecaPorIdUseCase(buscarPorIdGateway, logger).execute(id);
        logger.logInfo("Busca de peça por id concluída com sucesso - pecaId={}", id);
        return buscarPorIdPresenter.present(peca);
    }

    public PaginatedResult<PecaResponse> buscarTodos(int page, int size) {
        logger.logInfo("Iniciando busca de peças - page={}, size={}", page, size);
        var result = new BuscarPecasUseCase(buscarTodosGateway, logger).execute(page, size);
        logger.logInfo("Busca de peças concluída com sucesso - totalElements={}", result.totalElements());
        return buscarTodosPresenter.present(result);
    }

    public void desativar(Long id) {
        logger.logInfo("Iniciando desativação de peça - pecaId={}", id);
        new DesativarPecaUseCase(desativarGateway, logger).execute(id);
        logger.logInfo("Desativação de peça concluída com sucesso - pecaId={}", id);
    }
}
