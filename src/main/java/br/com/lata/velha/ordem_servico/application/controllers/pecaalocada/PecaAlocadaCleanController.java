package br.com.lata.velha.ordem_servico.application.controllers.pecaalocada;

import br.com.lata.velha.ordem_servico.application.dtos.response.PecaAlocadaResponse;
import br.com.lata.velha.ordem_servico.application.presenters.pecaalocada.BuscarPecaAlocadaPorIdPresenter;
import br.com.lata.velha.ordem_servico.application.presenters.pecaalocada.BuscarPecasAlocadasPresenter;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.BuscarPecaAlocadaPorIdGateway;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.BuscarPecaAlocadaPorIdUseCase;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.BuscarPecasAlocadasGateway;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.BuscarPecasAlocadasUseCase;
import br.com.lata.velha.shared.application.logging.Logger;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;

public class PecaAlocadaCleanController {

    private final BuscarPecaAlocadaPorIdGateway buscarPorIdGateway;
    private final BuscarPecaAlocadaPorIdPresenter buscarPorIdPresenter;

    private final BuscarPecasAlocadasGateway buscarTodosGateway;
    private final BuscarPecasAlocadasPresenter buscarTodosPresenter;

    private final Logger logger;

    public PecaAlocadaCleanController(BuscarPecaAlocadaPorIdGateway buscarPorIdGateway,
                                      BuscarPecaAlocadaPorIdPresenter buscarPorIdPresenter,
                                      BuscarPecasAlocadasGateway buscarTodosGateway,
                                      BuscarPecasAlocadasPresenter buscarTodosPresenter,
                                      Logger logger) {
        this.buscarPorIdGateway = buscarPorIdGateway;
        this.buscarPorIdPresenter = buscarPorIdPresenter;
        this.buscarTodosGateway = buscarTodosGateway;
        this.buscarTodosPresenter = buscarTodosPresenter;
        this.logger = logger;
    }

    public PecaAlocadaResponse buscarPorId(Long id) {
        logger.logInfo("Iniciando busca de peça alocada por id - pecaAlocadaId={}", id);
        var pecaAlocada = new BuscarPecaAlocadaPorIdUseCase(buscarPorIdGateway, logger).execute(id);
        logger.logInfo("Busca de peça alocada por id concluída com sucesso - pecaAlocadaId={}", id);
        return buscarPorIdPresenter.present(pecaAlocada);
    }

    public PaginatedResult<PecaAlocadaResponse> buscarTodos(Long execucaoServicoId, int page, int size) {
        logger.logInfo("Iniciando busca de peças alocadas - execucaoServicoId={}, page={}, size={}", execucaoServicoId, page, size);
        var result = new BuscarPecasAlocadasUseCase(buscarTodosGateway, logger).execute(execucaoServicoId, page, size);
        logger.logInfo("Busca de peças alocadas concluída com sucesso - totalElements={}", result.totalElements());
        return buscarTodosPresenter.present(result);
    }
}
