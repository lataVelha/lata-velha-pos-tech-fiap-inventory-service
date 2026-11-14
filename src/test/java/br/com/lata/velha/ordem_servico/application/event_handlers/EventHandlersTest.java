package br.com.lata.velha.ordem_servico.application.event_handlers;

import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.ReceberOrcamentoAprovadoUseCase;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque.ReceberServiceEndedUseCase;
import br.com.lata.velha.ordem_servico.domain.events.OrcamentoAprovadoEvent;
import br.com.lata.velha.ordem_servico.domain.events.ServiceEndedEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventHandlersTest {

    @Mock
    private ReceberOrcamentoAprovadoUseCase receberOrcamentoAprovadoUseCase;

    @Mock
    private ReceberServiceEndedUseCase receberServiceEndedUseCase;

    @Test
    @DisplayName("OrcamentoAprovadoEventHandler deve identificar e delegar o evento")
    void deveTratarOrcamentoAprovado() {
        var handler = new OrcamentoAprovadoEventHandler(receberOrcamentoAprovadoUseCase);
        var event = new OrcamentoAprovadoEvent(1L, List.of());

        handler.handle(event);

        assertThat(handler.eventType()).isEqualTo("OrcamentoAprovado");
        assertThat(handler.eventClass()).isEqualTo(OrcamentoAprovadoEvent.class);
        verify(receberOrcamentoAprovadoUseCase).execute(event);
    }

    @Test
    @DisplayName("ServiceEndedEventHandler deve identificar e delegar o evento")
    void deveTratarServiceEnded() {
        var handler = new ServiceEndedEventHandler(receberServiceEndedUseCase);
        var event = new ServiceEndedEvent(1L, 10L, List.of());

        handler.handle(event);

        assertThat(handler.eventType()).isEqualTo("ServiceEnded");
        assertThat(handler.eventClass()).isEqualTo(ServiceEndedEvent.class);
        verify(receberServiceEndedUseCase).execute(event);
    }
}
