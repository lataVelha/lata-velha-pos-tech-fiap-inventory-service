package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.application.dtos.request.MovimentarPecaEstoqueRequest;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.ordem_servico.domain.exceptions.not_found_exceptions.PecaNotFoundException;
import br.com.lata.velha.shared.application.gateways.EventPublisher;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntradaPecaEstoqueUseCaseTest {

    @Mock
    private EntradaPecaEstoqueGateway gateway;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private Logger logger;

    private static final Long PECA_ID = 1L;

    @Test
    @DisplayName("deve adicionar quantidade quando estoque já existe")
    void deveAdicionarQuantidadeQuandoEstoqueJaExiste() {
        PecaEstoque estoque = new PecaEstoque(PECA_ID, 10, 10);

        when(gateway.getEstoquePorPecaId(PECA_ID)).thenReturn(estoque);
        when(gateway.salvarEstoque(any())).thenAnswer(i -> i.getArgument(0));

        EntradaPecaEstoqueUseCase useCase = new EntradaPecaEstoqueUseCase(gateway, eventPublisher, logger);
        PecaEstoque response = useCase.execute(PECA_ID, new MovimentarPecaEstoqueRequest(5));

        assertThat(response.getQuantidadeArmazenada()).isEqualTo(15);
        assertThat(response.getQuantidadeDisponivel()).isEqualTo(15);
    }

    @Test
    @DisplayName("deve criar estoque quando gateway retorna estoque vazio")
    void deveCriarEstoqueQuandoGatewayRetornaVazio() {
        PecaEstoque estoqueVazio = new PecaEstoque(PECA_ID, 0, 0);

        when(gateway.getEstoquePorPecaId(PECA_ID)).thenReturn(estoqueVazio);
        when(gateway.salvarEstoque(any())).thenAnswer(i -> i.getArgument(0));

        EntradaPecaEstoqueUseCase useCase = new EntradaPecaEstoqueUseCase(gateway, eventPublisher, logger);
        PecaEstoque response = useCase.execute(PECA_ID, new MovimentarPecaEstoqueRequest(5));

        assertThat(response.getQuantidadeArmazenada()).isEqualTo(5);
        assertThat(response.getQuantidadeDisponivel()).isEqualTo(5);
    }

    @Test
    @DisplayName("deve lançar exceção quando peça não existe")
    void deveLancarExcecaoQuandoPecaNaoExiste() {
        when(gateway.getPecaAtivaPorId(99L)).thenThrow(PecaNotFoundException.fromId(99L));
        var request = new MovimentarPecaEstoqueRequest(5);

        EntradaPecaEstoqueUseCase useCase = new EntradaPecaEstoqueUseCase(gateway, eventPublisher, logger);

        assertThatThrownBy(() -> useCase.execute(99L, request))
                .isInstanceOf(PecaNotFoundException.class);
    }
}
