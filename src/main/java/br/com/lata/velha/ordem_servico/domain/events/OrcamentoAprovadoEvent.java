package br.com.lata.velha.ordem_servico.domain.events;

import br.com.lata.velha.shared.domain.events.AbstractDomainEvent;

import java.util.List;

public final class OrcamentoAprovadoEvent extends AbstractDomainEvent {

    private final Long osId;
    private final List<Item> itens;

    public OrcamentoAprovadoEvent(Long osId, List<Item> itens) {
        this.osId = osId;
        this.itens = itens;
    }

    @Override
    public String eventType() {
        return "OrcamentoAprovado";
    }

    public Long getOsId() {
        return osId;
    }

    public List<Item> getItens() {
        return itens;
    }

    public record Item(Long execucaoServicoId, Long pecaId, Integer quantidade) {
    }
}
