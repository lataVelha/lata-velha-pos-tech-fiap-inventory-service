package br.com.lata.velha.ordem_servico.application.controllers.pecaestoque;

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

    public PecaEstoqueCleanController(BuscarPecaEstoqueGateway buscarGateway,
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
        var estoque = new BuscarPecaEstoqueUseCase(buscarGateway, logger).execute(pecaId);
        logger.logInfo("Busca de estoque de peça concluída com sucesso - pecaId={}", pecaId);
        return buscarPresenter.present(estoque);
    }

    public PecaEstoqueResponse entrada(Long pecaId, MovimentarPecaEstoqueRequest request) {
        logger.logInfo("Iniciando entrada de estoque de peça - pecaId={}, quantidade={}", pecaId, request.quantidade());
        var estoque = new EntradaPecaEstoqueUseCase(entradaGateway, logger).execute(pecaId, request);
        logger.logInfo("Entrada de estoque de peça concluída com sucesso - pecaId={}", pecaId);
        return entradaPresenter.present(estoque);
    }

    public PecaEstoqueResponse saida(Long pecaId, MovimentarPecaEstoqueRequest request) {
        logger.logInfo("Iniciando saída de estoque de peça - pecaId={}, quantidade={}", pecaId, request.quantidade());
        var estoque = new SaidaPecaEstoqueUseCase(saidaGateway, logger).execute(pecaId, request);
        logger.logInfo("Saída de estoque de peça concluída com sucesso - pecaId={}", pecaId);
        return saidaPresenter.present(estoque);
    }

    public PecaEstoqueResponse ajustar(Long pecaId, AjustarPecaEstoqueRequest request) {
        logger.logInfo("Iniciando ajuste de estoque de peça - pecaId={}, quantidadeArmazenada={}, quantidadeDisponivel={}",
                pecaId, request.quantidadeArmazenada(), request.quantidadeDisponivel());
        var estoque = new AjustarPecaEstoqueUseCase(ajustarGateway, logger).execute(pecaId, request);
        logger.logInfo("Ajuste de estoque de peça concluído com sucesso - pecaId={}", pecaId);
        return ajustarPresenter.present(estoque);
    }
}
