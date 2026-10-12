package br.com.lata.velha.ordem_servico.application.use_cases.peca;

import br.com.lata.velha.ordem_servico.application.dtos.request.CadastrarPecaRequest;
import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.shared.application.logging.Logger;

public class CadastrarPecaUseCase {

    private final CadastrarPecaGateway gateway;
    private final Logger logger;

    public CadastrarPecaUseCase(CadastrarPecaGateway gateway, Logger logger) {
        this.gateway = gateway;
        this.logger = logger;
    }

    public Peca execute(CadastrarPecaRequest request) {
        logger.logInfo("Salvando peça - nome={}", request.nome());
        Peca saved = gateway.salvarPeca(request.toDomain());

        logger.logInfo("Criando registro de estoque inicial - pecaId={}", saved.getId());
        gateway.salvarEstoque(PecaEstoque.create(saved.getId()));
        logger.logInfo("Peça cadastrada com sucesso - pecaId={}", saved.getId());
        return saved;
    }
}
