package br.com.lata.velha.ordem_servico.domain.events;

import br.com.lata.velha.shared.infrastructure.messaging.EventSerializer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Contrato de OrcamentoAprovado (Story 14.2 / 15.1).
 *
 * Este JSON é idêntico ao produzido pelo billing-service
 * (OrcamentoAprovadoContractTest). O `valorTotal` extra é ignorado pelo
 * inventory (FAIL_ON_UNKNOWN_PROPERTIES=false) e existe para compatibilidade
 * com o os-service.
 */
class OrcamentoAprovadoContractTest {

    private final EventSerializer serializer = new EventSerializer();

    private String fixture() {
        return """
                {
                  "eventId": "%s",
                  "eventType": "OrcamentoAprovado",
                  "occurredAt": "%s",
                  "payload": {
                    "osId": 42,
                    "valorTotal": 250.00,
                    "itens": [
                      { "execucaoServicoId": 10, "pecaId": 100, "quantidade": 2 }
                    ]
                  }
                }
                """.formatted(UUID.randomUUID(), Instant.now());
    }

    @Test
    @DisplayName("inventory desserializa itens[] e ignora valorTotal (campo do os-service)")
    void deveDesserializarPayloadDoBilling() {
        var envelope = serializer.deserializeEnvelope(fixture());

        var event = serializer.deserializePayload(envelope, OrcamentoAprovadoEvent.class);

        assertThat(event.getOsId()).isEqualTo(42L);
        assertThat(event.getItens()).hasSize(1);
        var item = event.getItens().getFirst();
        assertThat(item.execucaoServicoId()).isEqualTo(10L);
        assertThat(item.pecaId()).isEqualTo(100L);
        assertThat(item.quantidade()).isEqualTo(2);
    }

    @Test
    @DisplayName("itens ausentes resultam em lista vazia, sem NullPointerException")
    void deveTolerarPayloadAntigoSemItens() {
        var json = """
                {
                  "eventId": "%s",
                  "eventType": "OrcamentoAprovado",
                  "occurredAt": "%s",
                  "payload": { "osId": 42, "valorTotal": 250.00 }
                }
                """.formatted(UUID.randomUUID(), Instant.now());

        var event = serializer.deserializePayload(serializer.deserializeEnvelope(json),
                OrcamentoAprovadoEvent.class);

        assertThat(event.getItens()).isEmpty();
    }
}
