package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.ordem_servico.domain.exceptions.not_found_exceptions.PecaNotFoundException;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarPecaEstoqueUseCaseTest {

    @Mock
    private BuscarPecaEstoqueGateway gateway;

    @Mock
    private Logger logger;

    @Test
    void deveBuscarEstoqueComSucesso() {
        PecaEstoque estoque = new PecaEstoque("1", 10, 10);

        when(gateway.getEstoquePorPecaId("1")).thenReturn(estoque);

        BuscarPecaEstoqueUseCase useCase = new BuscarPecaEstoqueUseCase(gateway, logger);
        PecaEstoque result = useCase.execute("1");

        assertThat(result.getPecaId()).isEqualTo("1");
        assertThat(result.getQuantidadeArmazenada()).isEqualTo(10);
    }

    @Test
    void deveLancarExcecaoQuandoPecaNaoExiste() {
        when(gateway.getPecaAtivaPorId("1")).thenThrow(PecaNotFoundException.fromId("1"));

        BuscarPecaEstoqueUseCase useCase = new BuscarPecaEstoqueUseCase(gateway, logger);

        assertThatThrownBy(() -> useCase.execute("1"))
                .isInstanceOf(PecaNotFoundException.class);
    }
}
