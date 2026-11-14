package br.com.lata.velha.ordem_servico.domain.events;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DomainEventsTest {

    @Test
    @DisplayName("InventoryCheckedEvent deve expor os dados e o tipo do evento")
    void deveExporInventoryCheckedEvent() {
        var item = new InventoryCheckedEvent.ItemDisponibilidade(10L, true);
        var event = new InventoryCheckedEvent(1L, List.of(item));

        assertThat(event.eventType()).isEqualTo("InventoryChecked");
        assertThat(event.getOsId()).isEqualTo(1L);
        assertThat(event.getItens()).containsExactly(item);
        assertThat(item.execucaoServicoId()).isEqualTo(10L);
        assertThat(item.pecasDisponiveis()).isTrue();
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    @DisplayName("DeducaoEstoqueFalhouEvent deve expor os dados e o tipo do evento")
    void deveExporDeducaoEstoqueFalhouEvent() {
        var event = new DeducaoEstoqueFalhouEvent(1L, 10L, "estoque insuficiente");

        assertThat(event.eventType()).isEqualTo("DeducaoEstoqueFalhou");
        assertThat(event.getOsId()).isEqualTo(1L);
        assertThat(event.getExecucaoServicoId()).isEqualTo(10L);
        assertThat(event.getMotivo()).isEqualTo("estoque insuficiente");
    }

    @Test
    @DisplayName("PecaCompletamenteAlocadaEvent deve expor os dados e o tipo do evento")
    void deveExporPecaCompletamenteAlocadaEvent() {
        var event = new PecaCompletamenteAlocadaEvent(10L);

        assertThat(event.eventType()).isEqualTo("PecaCompletamenteAlocada");
        assertThat(event.getExecucaoServicoId()).isEqualTo(10L);
    }

    @Test
    @DisplayName("ServiceEndedEvent deve expor os dados e o tipo do evento")
    void deveExporServiceEndedEvent() {
        var item = new ServiceEndedEvent.ItemPeca("10", 2);
        var event = new ServiceEndedEvent(1L, 10L, List.of(item));

        assertThat(event.eventType()).isEqualTo("ServiceEnded");
        assertThat(event.getOsId()).isEqualTo(1L);
        assertThat(event.getExecucaoServicoId()).isEqualTo(10L);
        assertThat(event.getItens()).containsExactly(item);
        assertThat(item.pecaId()).isEqualTo("10");
        assertThat(item.quantidade()).isEqualTo(2);
    }

    @Test
    @DisplayName("OrcamentoAprovadoEvent deve copiar itens e tolerar lista nula")
    void deveExporOrcamentoAprovadoEvent() {
        var item = new OrcamentoAprovadoEvent.Item(10L, "10", 2);
        var event = new OrcamentoAprovadoEvent(1L, List.of(item));

        assertThat(event.eventType()).isEqualTo("OrcamentoAprovado");
        assertThat(event.getOsId()).isEqualTo(1L);
        assertThat(event.getItens()).containsExactly(item);
        assertThat(item.execucaoServicoId()).isEqualTo(10L);
        assertThat(item.pecaId()).isEqualTo("10");
        assertThat(item.quantidade()).isEqualTo(2);
        assertThat(new OrcamentoAprovadoEvent(1L, null).getItens()).isEmpty();
    }
}
