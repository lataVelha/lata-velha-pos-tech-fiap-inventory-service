package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import br.com.lata.velha.ordem_servico.domain.events.DeducaoEstoqueFalhouEvent;
import br.com.lata.velha.ordem_servico.domain.events.ServiceEndedEvent;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReceberServiceEndedUseCaseTest {

    private static final Long EXECUCAO_ID = 10L;
    private static final Long OS_ID = 1L;

    @Mock
    private DeduzirEstoqueExecucaoUseCase deduzirEstoqueExecucaoUseCase;

    @Mock
    private ReceberServiceEndedGateway gateway;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Logger logger;

    private ReceberServiceEndedUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ReceberServiceEndedUseCase(deduzirEstoqueExecucaoUseCase, gateway, eventPublisher, logger);
    }

    private PecaAlocada alocada(String id, String pecaId, StatusPecaAlocada status, int solicitada, int reservada, int instalada) {
        return new PecaAlocada(id, pecaId, EXECUCAO_ID, BigDecimal.TEN, solicitada, reservada, 0, instalada, status, LocalDateTime.now());
    }

    private ServiceEndedEvent evento(ServiceEndedEvent.ItemPeca... itens) {
        return new ServiceEndedEvent(OS_ID, EXECUCAO_ID, List.of(itens));
    }

    @Test
    @DisplayName("deve deduzir estoque e marcar peças como instaladas")
    void deveDeduzirEstoqueEMarcarInstaladas() {
        var filtro = alocada("1", "10", StatusPecaAlocada.RESERVADA, 2, 2, 0);
        var oleo = alocada("2", "20", StatusPecaAlocada.RESERVADA, 1, 1, 0);

        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "10")).thenReturn(Optional.of(filtro));
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "20")).thenReturn(Optional.of(oleo));

        useCase.execute(evento(new ServiceEndedEvent.ItemPeca("10", 2), new ServiceEndedEvent.ItemPeca("20", 1)));

        var inputCaptor = ArgumentCaptor.forClass(DeduzirEstoqueExecucaoUseCase.Input.class);
        verify(deduzirEstoqueExecucaoUseCase).execute(inputCaptor.capture());
        assertThat(inputCaptor.getValue().execucaoServicoId()).isEqualTo(EXECUCAO_ID);
        assertThat(inputCaptor.getValue().itens()).hasSize(2);

        verify(gateway).salvarPecaAlocada(filtro);
        verify(gateway).salvarPecaAlocada(oleo);
        assertThat(filtro.getQuantidadeInstalada()).isEqualTo(2);
        assertThat(filtro.getStatus()).isEqualTo(StatusPecaAlocada.INSTALADA);
        assertThat(oleo.getStatus()).isEqualTo(StatusPecaAlocada.INSTALADA);
        verifyNoInteractions(eventPublisher);
    }

    @Test
    @DisplayName("deve publicar falha quando peça nunca foi reservada")
    void devePublicarFalhaQuandoPecaNaoReservada() {
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "10")).thenReturn(Optional.empty());
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "20"))
                .thenReturn(Optional.of(alocada("2", "20", StatusPecaAlocada.RESERVADA, 1, 1, 0)));

        useCase.execute(evento(new ServiceEndedEvent.ItemPeca("10", 1), new ServiceEndedEvent.ItemPeca("20", 1)));

        var captor = ArgumentCaptor.forClass(DeducaoEstoqueFalhouEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().eventType()).isEqualTo("DeducaoEstoqueFalhou");
        assertThat(captor.getValue().getOsId()).isEqualTo(OS_ID);
        assertThat(captor.getValue().getExecucaoServicoId()).isEqualTo(EXECUCAO_ID);
        assertThat(captor.getValue().getMotivo()).contains("10");
        verifyNoInteractions(deduzirEstoqueExecucaoUseCase);
        verify(gateway, never()).salvarPecaAlocada(any());
    }

    @Test
    @DisplayName("não deve deduzir quando todas as peças já estão instaladas")
    void naoDeveDeduzirQuandoJaProcessado() {
        var instalada = alocada("1", "10", StatusPecaAlocada.INSTALADA, 2, 2, 2);
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "10")).thenReturn(Optional.of(instalada));

        useCase.execute(evento(new ServiceEndedEvent.ItemPeca("10", 2)));

        verifyNoInteractions(deduzirEstoqueExecucaoUseCase);
        verifyNoInteractions(eventPublisher);
        verify(gateway, never()).salvarPecaAlocada(any());
    }

    @Test
    @DisplayName("deve deduzir apenas as peças ainda pendentes")
    void deveDeduzirApenasPendentes() {
        var instalada = alocada("1", "10", StatusPecaAlocada.INSTALADA, 1, 1, 1);
        var pendente = alocada("2", "20", StatusPecaAlocada.RESERVADA, 1, 1, 0);
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "10")).thenReturn(Optional.of(instalada));
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "20")).thenReturn(Optional.of(pendente));

        useCase.execute(evento(new ServiceEndedEvent.ItemPeca("10", 1), new ServiceEndedEvent.ItemPeca("20", 1)));

        var inputCaptor = ArgumentCaptor.forClass(DeduzirEstoqueExecucaoUseCase.Input.class);
        verify(deduzirEstoqueExecucaoUseCase).execute(inputCaptor.capture());
        assertThat(inputCaptor.getValue().itens()).hasSize(1);
        assertThat(inputCaptor.getValue().itens().getFirst().pecaId()).isEqualTo("20");
        verify(gateway).salvarPecaAlocada(pendente);
        verify(gateway, never()).salvarPecaAlocada(instalada);
    }

    @Test
    @DisplayName("deve publicar falha quando a dedução lança IllegalArgumentException")
    void devePublicarFalhaQuandoDeducaoIllegalArgument() {
        var peca = alocada("1", "10", StatusPecaAlocada.RESERVADA, 2, 2, 0);
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "10")).thenReturn(Optional.of(peca));
        doThrow(new IllegalArgumentException("estoque insuficiente"))
                .when(deduzirEstoqueExecucaoUseCase).execute(any());

        useCase.execute(evento(new ServiceEndedEvent.ItemPeca("10", 2)));

        var captor = ArgumentCaptor.forClass(DeducaoEstoqueFalhouEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getMotivo()).isEqualTo("estoque insuficiente");
        verify(gateway, never()).salvarPecaAlocada(any());
    }

    @Test
    @DisplayName("deve publicar falha quando a dedução lança IllegalStateException")
    void devePublicarFalhaQuandoDeducaoIllegalState() {
        var peca = alocada("1", "10", StatusPecaAlocada.RESERVADA, 2, 2, 0);
        when(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, "10")).thenReturn(Optional.of(peca));
        doThrow(new IllegalStateException("sem registro de estoque"))
                .when(deduzirEstoqueExecucaoUseCase).execute(any());

        useCase.execute(evento(new ServiceEndedEvent.ItemPeca("10", 2)));

        var captor = ArgumentCaptor.forClass(DeducaoEstoqueFalhouEvent.class);
        verify(eventPublisher).publish(captor.capture());
        assertThat(captor.getValue().getMotivo()).isEqualTo("sem registro de estoque");
    }
}
