package br.com.lata.velha.ordem_servico.application.event_handlers;

import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.ReceberOrcamentoAprovadoUseCase;
import br.com.lata.velha.ordem_servico.domain.events.OrcamentoAprovadoEvent;
import br.com.lata.velha.shared.application.gateways.EventHandler;

public class OrcamentoAprovadoEventHandler implements EventHandler<OrcamentoAprovadoEvent> {

    private final ReceberOrcamentoAprovadoUseCase useCase;

    public OrcamentoAprovadoEventHandler(ReceberOrcamentoAprovadoUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public String eventType() {
        return "OrcamentoAprovado";
    }

    @Override
    public Class<OrcamentoAprovadoEvent> eventClass() {
        return OrcamentoAprovadoEvent.class;
    }

    @Override
    public void handle(OrcamentoAprovadoEvent event) {
        useCase.execute(event);
    }
}
