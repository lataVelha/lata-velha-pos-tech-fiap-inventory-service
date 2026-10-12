package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.application.dtos.request.MovimentarPecaEstoqueRequest;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.ordem_servico.domain.exceptions.not_found_exceptions.PecaNotFoundException;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaidaPecaEstoqueUseCaseTest {

    @Mock
    private SaidaPecaEstoqueGateway gateway;

    @Mock
    private Logger logger;

    @Test
    void deveBaixarEstoqueComSucesso() {
        PecaEstoque estoque = new PecaEstoque(1L, 10, 10);
        MovimentarPecaEstoqueRequest request = new MovimentarPecaEstoqueRequest(3);

        when(gateway.getEstoquePorPecaId(1L)).thenReturn(estoque);
        when(gateway.salvarEstoque(any(PecaEstoque.class))).thenAnswer(i -> i.getArgument(0));

        SaidaPecaEstoqueUseCase useCase = new SaidaPecaEstoqueUseCase(gateway, logger);
        PecaEstoque response = useCase.execute(1L, request);

        assertThat(response.getQuantidadeArmazenada()).isEqualTo(7);
    }

    @Test
    void deveLancarExcecaoQuandoEstoqueInsuficiente() {
        PecaEstoque estoque = new PecaEstoque(1L, 2, 2);
        MovimentarPecaEstoqueRequest request = new MovimentarPecaEstoqueRequest(3);

        when(gateway.getEstoquePorPecaId(1L)).thenReturn(estoque);

        SaidaPecaEstoqueUseCase useCase = new SaidaPecaEstoqueUseCase(gateway, logger);

        assertThatThrownBy(() -> useCase.execute(1L, request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Estoque insuficiente para a peça informada");
    }

    @Test
    void deveLancarExcecaoQuandoPecaNaoExiste() {
        MovimentarPecaEstoqueRequest request = new MovimentarPecaEstoqueRequest(3);

        when(gateway.getPecaAtivaPorId(1L)).thenThrow(PecaNotFoundException.fromId(1L));

        SaidaPecaEstoqueUseCase useCase = new SaidaPecaEstoqueUseCase(gateway, logger);

        assertThatThrownBy(() -> useCase.execute(1L, request))
                .isInstanceOf(PecaNotFoundException.class);
    }
}
