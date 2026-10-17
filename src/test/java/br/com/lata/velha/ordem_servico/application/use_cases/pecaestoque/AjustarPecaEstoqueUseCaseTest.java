package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.application.dtos.request.AjustarPecaEstoqueRequest;
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
class AjustarPecaEstoqueUseCaseTest {

    @Mock
    private AjustarPecaEstoqueGateway gateway;

    @Mock
    private Logger logger;

    @Test
    void deveAjustarSaldoComSucesso() {
        PecaEstoque estoque = new PecaEstoque(1L, 10, 10);
        AjustarPecaEstoqueRequest request = new AjustarPecaEstoqueRequest(4, 4);

        when(gateway.getEstoquePorPecaId(1L)).thenReturn(estoque);
        when(gateway.salvarEstoque(any(PecaEstoque.class))).thenAnswer(i -> i.getArgument(0));

        AjustarPecaEstoqueUseCase useCase = new AjustarPecaEstoqueUseCase(gateway, logger);
        PecaEstoque response = useCase.execute(1L, request);

        assertThat(response.getQuantidadeArmazenada()).isEqualTo(4);
        assertThat(response.getQuantidadeDisponivel()).isEqualTo(4);
    }

    @Test
    void deveAjustarEstoqueInicialQuandoGatewayRetornaVazio() {
        PecaEstoque estoqueVazio = new PecaEstoque(1L, 0, 0);
        AjustarPecaEstoqueRequest request = new AjustarPecaEstoqueRequest(10, 5);

        when(gateway.getEstoquePorPecaId(1L)).thenReturn(estoqueVazio);
        when(gateway.salvarEstoque(any(PecaEstoque.class))).thenAnswer(i -> i.getArgument(0));

        AjustarPecaEstoqueUseCase useCase = new AjustarPecaEstoqueUseCase(gateway, logger);
        PecaEstoque response = useCase.execute(1L, request);

        assertThat(response.getQuantidadeArmazenada()).isEqualTo(10);
        assertThat(response.getQuantidadeDisponivel()).isEqualTo(5);
    }

    @Test
    void deveLancarExcecaoQuandoPecaNaoExiste() {
        AjustarPecaEstoqueRequest request = new AjustarPecaEstoqueRequest(10, 5);

        when(gateway.getPecaAtivaPorId(99L)).thenThrow(PecaNotFoundException.fromId(99L));

        AjustarPecaEstoqueUseCase useCase = new AjustarPecaEstoqueUseCase(gateway, logger);

        assertThatThrownBy(() -> useCase.execute(99L, request))
                .isInstanceOf(PecaNotFoundException.class);
    }
}
