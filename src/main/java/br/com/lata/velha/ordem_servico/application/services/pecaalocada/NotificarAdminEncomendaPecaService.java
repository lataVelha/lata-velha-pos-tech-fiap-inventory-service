package br.com.lata.velha.ordem_servico.application.services.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.application.gateways.EmailProvider;
import br.com.lata.velha.shared.application.gateways.EmailTemplateProvider;
import br.com.lata.velha.shared.application.logging.Logger;

import java.util.HashMap;
import java.util.Map;

public class NotificarAdminEncomendaPecaService {

    private final NotificarAdminEncomendaPecaGateway gateway;
    private final EmailProvider emailProvider;
    private final EmailTemplateProvider templateProvider;
    private final Logger logger;

    public NotificarAdminEncomendaPecaService(NotificarAdminEncomendaPecaGateway gateway,
                                              EmailProvider emailProvider,
                                              EmailTemplateProvider templateProvider,
                                              Logger logger) {
        this.gateway = gateway;
        this.emailProvider = emailProvider;
        this.templateProvider = templateProvider;
        this.logger = logger;
    }

    public record Input(Long execucaoServicoId, Long pecaId, Integer quantidade) {}

    public void execute(Input input) {
        logger.logInfo("Notificando admins sobre encomenda de peça - execucaoServicoId={}, pecaId={}, quantidade={}",
                input.execucaoServicoId(), input.pecaId(), input.quantidade());

        Peca peca;
        try {
            var pecaOpt = gateway.findPecaPorId(input.pecaId());
            if (pecaOpt.isEmpty()) {
                logger.logWarn("Peça não encontrada ao tentar notificar encomenda - pecaId={}", input.pecaId());
                return;
            }
            peca = pecaOpt.get();
        } catch (Exception e) {
            logger.logError("Falha ao verificar peça para notificação de encomenda - pecaId=" + input.pecaId(), e);
            return;
        }

        var adminEmails = gateway.getAdminEmails();
        if (adminEmails.isEmpty()) {
            logger.logWarn("Nenhum e-mail de admin configurado para notificar encomenda de peça - execucaoServicoId={}", input.execucaoServicoId());
            return;
        }

        adminEmails.forEach(email -> {
            try {
                var html = templateProvider.render("peca-encomenda", buildVariables(input, peca));
                emailProvider.send(email, "Encomenda de Peça — Execução Nº " + input.execucaoServicoId(), html);
            } catch (Exception e) {
                logger.logError("Falha ao notificar admin sobre encomenda de peça - execucaoServicoId=" + input.execucaoServicoId() + ", email=" + email, e);
            }
        });
    }

    private Map<String, Object> buildVariables(Input input, Peca peca) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("execucaoServicoId", input.execucaoServicoId());
        vars.put("pecaNome", peca.getNome());
        vars.put("quantidade", input.quantidade());
        return vars;
    }
}
