package br.com.lata.velha.ordem_servico.domain.events;

import br.com.lata.velha.shared.domain.events.AbstractDomainEvent;

import java.util.List;

public final class ServiceEndedEvent extends AbstractDomainEvent {

    private final Long osId;
    private final Long execucaoServicoId;
    private final List<ItemPeca> itens;

    public ServiceEndedEvent(Long osId, Long execucaoServicoId, List<ItemPeca> itens) {
        this.osId = osId;
        this.execucaoServicoId = execucaoServicoId;
        this.itens = itens;
    }

    @Override
    public String eventType() {
        return "ServiceEnded";
    }

    public Long getOsId() {
        return osId;
    }

    public Long getExecucaoServicoId() {
        return execucaoServicoId;
    }

    public List<ItemPeca> getItens() {
        return itens;
    }

    public record ItemPeca(Long pecaId, Integer quantidade) {
    }
}
