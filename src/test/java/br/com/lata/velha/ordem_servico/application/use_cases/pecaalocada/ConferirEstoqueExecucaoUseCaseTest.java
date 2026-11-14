package br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada;

import br.com.lata.velha.ordem_servico.application.services.pecaalocada.NotificarAdminEncomendaPecaService;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConferirEstoqueExecucaoUseCaseTest {

    private static final Long EXECUCAO_ID = 10L;
    private static final String PECA_ID = "10";
    private static final BigDecimal VALOR = new BigDecimal("35.00");

    @Mock
    private ConferirEstoqueGateway gateway;

    @Mock
    private NotificarAdminEncomendaPecaService notificarAdminEncomendaService;

    @Mock
    private Logger logger;

    private ConferirEstoqueExecucaoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ConferirEstoqueExecucaoUseCase(gateway, notificarAdminEncomendaService, logger);
    }

    private ConferirEstoqueExecucaoUseCase.Input input(int quantidade) {
        return new ConferirEstoqueExecucaoUseCase.Input(List.of(
                new ConferirEstoqueExecucaoUseCase.Input.Item(EXECUCAO_ID, PECA_ID, quantidade, VALOR)));
    }

    @Test
    @DisplayName("deve reservar estoque e salvar a peça alocada quando há quantidade suficiente")
    void deveReservarEstoqueSuficiente() {
        var estoque = new PecaEstoque(PECA_ID, 10, 10);
        var alocada = PecaAlocada.create(PECA_ID, EXECUCAO_ID, VALOR, 5);
        when(gateway.getEstoquePorPecaIds(Set.of(PECA_ID))).thenReturn(List.of(estoque));
        when(gateway.getOuCriarPecaAlocada(EXECUCAO_ID, PECA_ID, 5, VALOR)).thenReturn(alocada);
        when(gateway.salvarPecaAlocada(alocada)).thenReturn(alocada);

        var resultado = useCase.execute(input(5));

        assertThat(resultado).containsExactly(alocada);
        assertThat(alocada.getStatus()).isEqualTo(StatusPecaAlocada.RESERVADA);
        assertThat(alocada.getQuantidadeReservada()).isEqualTo(5);
        assertThat(alocada.getQuantidadeEncomendada()).isZero();
        assertThat(estoque.getQuantidadeDisponivel()).isEqualTo(5);
        verifyNoInteractions(notificarAdminEncomendaService);

        @SuppressWarnings("unchecked")
        var captor = ArgumentCaptor.forClass(Collection.class);
        verify(gateway).salvarEstoques(captor.capture());
        assertThat(captor.getValue()).containsExactly(estoque);
    }

    @Test
    @DisplayName("deve notificar admin quando o estoque é insuficiente")
    void deveNotificarAdminQuandoEstoqueInsuficiente() {
        var estoque = new PecaEstoque(PECA_ID, 2, 2);
        var alocada = PecaAlocada.create(PECA_ID, EXECUCAO_ID, VALOR, 5);
        when(gateway.getEstoquePorPecaIds(Set.of(PECA_ID))).thenReturn(List.of(estoque));
        when(gateway.getOuCriarPecaAlocada(EXECUCAO_ID, PECA_ID, 5, VALOR)).thenReturn(alocada);
        when(gateway.salvarPecaAlocada(alocada)).thenReturn(alocada);

        useCase.execute(input(5));

        assertThat(alocada.getStatus()).isEqualTo(StatusPecaAlocada.PARCIAL);
        assertThat(alocada.getQuantidadeEncomendada()).isEqualTo(3);

        var captor = ArgumentCaptor.forClass(NotificarAdminEncomendaPecaService.Input.class);
        verify(notificarAdminEncomendaService).execute(captor.capture());
        assertThat(captor.getValue().execucaoServicoId()).isEqualTo(EXECUCAO_ID);
        assertThat(captor.getValue().pecaId()).isEqualTo(PECA_ID);
        assertThat(captor.getValue().quantidade()).isEqualTo(3);
    }

    @Test
    @DisplayName("deve lançar IllegalArgumentException quando a peça não tem registro de estoque")
    void deveFalharQuandoPecaSemEstoque() {
        when(gateway.getEstoquePorPecaIds(Set.of(PECA_ID))).thenReturn(List.of());

        var input = input(2);

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Peças sem registro de estoque");

        verify(gateway, never()).salvarPecaAlocada(any());
        verifyNoInteractions(notificarAdminEncomendaService);
    }
}
