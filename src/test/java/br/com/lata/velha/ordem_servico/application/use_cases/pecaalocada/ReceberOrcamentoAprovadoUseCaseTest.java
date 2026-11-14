package br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import br.com.lata.velha.ordem_servico.domain.events.InventoryCheckedEvent;
import br.com.lata.velha.ordem_servico.domain.events.OrcamentoAprovadoEvent;
import br.com.lata.velha.shared.application.gateways.EventPublisher;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceberOrcamentoAprovadoUseCaseTest {

    private static final Long OS_ID = 1L;
    private static final Long EXECUCAO_ID = 10L;

    @Mock
    private ConferirEstoqueExecucaoUseCase conferirEstoqueExecucaoUseCase;

    @Mock
    private ReceberOrcamentoAprovadoGateway gateway;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Logger logger;

    private ReceberOrcamentoAprovadoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReceberOrcamentoAprovadoUseCase(conferirEstoqueExecucaoUseCase, gateway, eventPublisher, logger);
    }

    private PecaAlocada alocada(String pecaId, StatusPecaAlocada status) {
        return new PecaAlocada("1", pecaId, EXECUCAO_ID, new BigDecimal("35.00"), 2, 2, 0, 0, status, LocalDateTime.now());
    }

    @Test
    @DisplayName("deve conferir estoque dos itens novos e publicar InventoryChecked")
    void deveConferirItensNovosEPublicarInventoryChecked() {
        var item = new OrcamentoAprovadoEvent.Item(EXECUCAO_ID, "10", 2);
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "10")).thenReturn(Optional.empty());
        when(gateway.getPecasPorIds(Set.of("10")))
                .thenReturn(List.of(new Peca("10", "Filtro", "Filtro de óleo", new BigDecimal("35.00"))));
        when(gateway.getTodasPecaAlocadaPorExecucaoServicoId(EXECUCAO_ID))
                .thenReturn(List.of(alocada("10", StatusPecaAlocada.RESERVADA)));

        useCase.execute(new OrcamentoAprovadoEvent(OS_ID, List.of(item)));

        var conferirCaptor = ArgumentCaptor.forClass(ConferirEstoqueExecucaoUseCase.Input.class);
        verify(conferirEstoqueExecucaoUseCase).execute(conferirCaptor.capture());
        assertThat(conferirCaptor.getValue().itens()).hasSize(1);
        assertThat(conferirCaptor.getValue().itens().getFirst().valorUnitarioPeca())
                .isEqualByComparingTo(new BigDecimal("35.00"));

        var eventCaptor = ArgumentCaptor.forClass(InventoryCheckedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().eventType()).isEqualTo("InventoryChecked");
        assertThat(eventCaptor.getValue().getOsId()).isEqualTo(OS_ID);
        assertThat(eventCaptor.getValue().getItens()).hasSize(1);
        assertThat(eventCaptor.getValue().getItens().getFirst().pecasDisponiveis()).isTrue();
    }

    @Test
    @DisplayName("deve marcar indisponível quando a peça alocada não está reservada nem instalada")
    void deveMarcarIndisponivelQuandoPecaPendente() {
        var item = new OrcamentoAprovadoEvent.Item(EXECUCAO_ID, "10", 2);
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "10"))
                .thenReturn(Optional.of(alocada("10", StatusPecaAlocada.PARCIAL)));
        when(gateway.getTodasPecaAlocadaPorExecucaoServicoId(EXECUCAO_ID))
                .thenReturn(List.of(alocada("10", StatusPecaAlocada.PARCIAL)));

        useCase.execute(new OrcamentoAprovadoEvent(OS_ID, List.of(item)));

        verifyNoInteractions(conferirEstoqueExecucaoUseCase);
        var eventCaptor = ArgumentCaptor.forClass(InventoryCheckedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getItens().getFirst().pecasDisponiveis()).isFalse();
    }

    @Test
    @DisplayName("deve considerar disponível quando a peça já foi instalada")
    void deveConsiderarDisponivelQuandoInstalada() {
        var item = new OrcamentoAprovadoEvent.Item(EXECUCAO_ID, "10", 2);
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "10"))
                .thenReturn(Optional.of(alocada("10", StatusPecaAlocada.INSTALADA)));
        when(gateway.getTodasPecaAlocadaPorExecucaoServicoId(EXECUCAO_ID))
                .thenReturn(List.of(alocada("10", StatusPecaAlocada.INSTALADA)));

        useCase.execute(new OrcamentoAprovadoEvent(OS_ID, List.of(item)));

        var eventCaptor = ArgumentCaptor.forClass(InventoryCheckedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getItens().getFirst().pecasDisponiveis()).isTrue();
    }

    @Test
    @DisplayName("deve agrupar itens da mesma execução em um único InventoryChecked")
    void deveAgruparItensDaMesmaExecucao() {
        var item1 = new OrcamentoAprovadoEvent.Item(EXECUCAO_ID, "10", 1);
        var item2 = new OrcamentoAprovadoEvent.Item(EXECUCAO_ID, "20", 1);
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "10"))
                .thenReturn(Optional.of(alocada("10", StatusPecaAlocada.RESERVADA)));
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "20"))
                .thenReturn(Optional.of(alocada("20", StatusPecaAlocada.RESERVADA)));
        when(gateway.getTodasPecaAlocadaPorExecucaoServicoId(EXECUCAO_ID))
                .thenReturn(List.of(alocada("10", StatusPecaAlocada.RESERVADA),
                        alocada("20", StatusPecaAlocada.RESERVADA)));

        useCase.execute(new OrcamentoAprovadoEvent(OS_ID, List.of(item1, item2)));

        verify(gateway, never()).getPecasPorIds(any());
        var eventCaptor = ArgumentCaptor.forClass(InventoryCheckedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getItens()).hasSize(1);
    }

    @Test
    @DisplayName("deve publicar InventoryChecked sem itens quando o evento não tem peças")
    void devePublicarSemItensQuandoEventoVazio() {
        useCase.execute(new OrcamentoAprovadoEvent(OS_ID, null));

        var eventCaptor = ArgumentCaptor.forClass(InventoryCheckedEvent.class);
        verify(eventPublisher).publish(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getItens()).isEmpty();
        verifyNoInteractions(conferirEstoqueExecucaoUseCase);
    }
}
