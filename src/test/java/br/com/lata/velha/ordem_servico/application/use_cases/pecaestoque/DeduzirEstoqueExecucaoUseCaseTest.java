package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DeduzirEstoqueExecucaoUseCaseTest {

    @Mock
    private DeduzirEstoqueGateway gateway;

    @Mock
    private Logger logger;

    private DeduzirEstoqueExecucaoUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new DeduzirEstoqueExecucaoUseCase(gateway, logger);
    }

    @Test
    @DisplayName("deve retirar as quantidades do estoque e salvar")
    void deveRetirarEstoqueESalvar() {
        var filtro = new PecaEstoque("10", 50, 50);
        var oleo = new PecaEstoque("20", 10, 10);
        when(gateway.getEstoquePorPecaIds(Set.of("10", "20"))).thenReturn(List.of(filtro, oleo));

        useCase.execute(new DeduzirEstoqueExecucaoUseCase.Input(1L,
                List.of(new DeduzirEstoqueExecucaoUseCase.Input.Item("10", 5),
                        new DeduzirEstoqueExecucaoUseCase.Input.Item("20", 2))));

        assertThat(filtro.getQuantidadeArmazenada()).isEqualTo(45);
        assertThat(oleo.getQuantidadeArmazenada()).isEqualTo(8);

        @SuppressWarnings("unchecked")
        var captor = org.mockito.ArgumentCaptor.forClass(Collection.class);
        verify(gateway).salvarEstoques(captor.capture());
        assertThat(captor.getValue()).containsExactlyInAnyOrder(filtro, oleo);
    }

    @Test
    @DisplayName("deve lançar IllegalStateException quando alguma peça não possui estoque")
    void deveFalharQuandoPecaSemEstoque() {
        when(gateway.getEstoquePorPecaIds(Set.of("10", "20"))).thenReturn(List.of(new PecaEstoque("10", 5, 5)));

        var input = new DeduzirEstoqueExecucaoUseCase.Input(1L,
                List.of(new DeduzirEstoqueExecucaoUseCase.Input.Item("10", 1),
                        new DeduzirEstoqueExecucaoUseCase.Input.Item("20", 1)));

        assertThatThrownBy(() -> useCase.execute(input))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nem todas as peças possuem registro de estoque");

        verify(gateway, never()).salvarEstoques(any());
    }
}
