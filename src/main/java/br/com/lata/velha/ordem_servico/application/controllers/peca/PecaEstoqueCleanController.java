package br.com.lata.velha.ordem_servico.application.controllers.peca;

import br.com.lata.velha.ordem_servico.application.dtos.request.AjustarPecaEstoqueRequest;
import br.com.lata.velha.ordem_servico.application.dtos.request.MovimentarPecaEstoqueRequest;
import br.com.lata.velha.ordem_servico.application.dtos.response.PecaEstoqueResponse;
import br.com.lata.velha.ordem_servico.application.presenters.pecaestoque.*;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque.*;
import br.com.lata.velha.shared.application.logging.Logger;

public class PecaEstoqueCleanController {

    private final BuscarPecaEstoqueGateway buscarGateway;
    private final BuscarPecaEstoquePresenter buscarPresenter;

    private final EntradaPecaEstoqueGateway entradaGateway;
    private final EntradaPecaEstoquePresenter entradaPresenter;

    private final SaidaPecaEstoqueGateway saidaGateway;
    private final SaidaPecaEstoquePresenter saidaPresenter;

    private final AjustarPecaEstoqueGateway ajustarGateway;
    private final AjustarPecaEstoquePresenter ajustarPresenter;

    private final Logger logger;

    public PecaEstoqueCleanController(
            BuscarPecaEstoqueGateway buscarGateway,
            BuscarPecaEstoquePresenter buscarPresenter,
            EntradaPecaEstoqueGateway entradaGateway,
            EntradaPecaEstoquePresenter entradaPresenter,
            SaidaPecaEstoqueGateway saidaGateway,
            SaidaPecaEstoquePresenter saidaPresenter,
            AjustarPecaEstoqueGateway ajustarGateway,
            AjustarPecaEstoquePresenter ajustarPresenter,
            Logger logger) {

        this.buscarGateway = buscarGateway;
        this.buscarPresenter = buscarPresenter;
        this.entradaGateway = entradaGateway;
        this.entradaPresenter = entradaPresenter;
        this.saidaGateway = saidaGateway;
        this.saidaPresenter = saidaPresenter;
        this.ajustarGateway = ajustarGateway;
        this.ajustarPresenter = ajustarPresenter;
        this.logger = logger;
    }

    public PecaEstoqueResponse buscar(Long pecaId) {
        logger.logInfo("Iniciando busca de estoque de peça - pecaId={}", pecaId);
        var result = new BuscarPecaEstoqueUseCase(buscarGateway, logger).execute(pecaId);
        logger.logInfo("Busca de estoque de peça concluída com sucesso - pecaId={}", pecaId);
        return buscarPresenter.present(result);
    }

    public PecaEstoqueResponse entrada(Long pecaId, MovimentarPecaEstoqueRequest request) {
        logger.logInfo("Iniciando entrada de estoque - pecaId={}, quantidade={}", pecaId, request.quantidade());
        var result = new EntradaPecaEstoqueUseCase(entradaGateway, logger).execute(pecaId, request);
        logger.logInfo("Entrada de estoque concluída com sucesso - pecaId={}", pecaId);
        return entradaPresenter.present(result);
    }

    public PecaEstoqueResponse saida(Long pecaId, MovimentarPecaEstoqueRequest request) {
        logger.logInfo("Iniciando saída de estoque - pecaId={}, quantidade={}", pecaId, request.quantidade());
        var result = new SaidaPecaEstoqueUseCase(saidaGateway, logger).execute(pecaId, request);
        logger.logInfo("Saída de estoque concluída com sucesso - pecaId={}", pecaId);
        return saidaPresenter.present(result);
    }

    public PecaEstoqueResponse ajustar(Long pecaId, AjustarPecaEstoqueRequest request) {
        logger.logInfo("Iniciando ajuste de estoque - pecaId={}", pecaId);
        var result = new AjustarPecaEstoqueUseCase(ajustarGateway, logger).execute(pecaId, request);
        logger.logInfo("Ajuste de estoque concluído com sucesso - pecaId={}", pecaId);
        return ajustarPresenter.present(result);
    }
}
