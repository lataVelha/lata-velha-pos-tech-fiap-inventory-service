package br.com.lata.velha.ordem_servico.application.event_handlers;

import br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque.ReceberServiceEndedUseCase;
import br.com.lata.velha.ordem_servico.domain.events.ServiceEndedEvent;
import br.com.lata.velha.shared.application.gateways.EventHandler;

public class ServiceEndedEventHandler implements EventHandler<ServiceEndedEvent> {

    private final ReceberServiceEndedUseCase useCase;

    public ServiceEndedEventHandler(ReceberServiceEndedUseCase useCase) {
        this.useCase = useCase;
    }

    @Override
    public String eventType() {
        return "ServiceEnded";
    }

    @Override
    public Class<ServiceEndedEvent> eventClass() {
        return ServiceEndedEvent.class;
    }

    @Override
    public void handle(ServiceEndedEvent event) {
        useCase.execute(event);
    }
}
