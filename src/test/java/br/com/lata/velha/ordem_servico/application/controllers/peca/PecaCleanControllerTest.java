package br.com.lata.velha.ordem_servico.application.controllers.peca;

import br.com.lata.velha.ordem_servico.application.dtos.request.AtualizarPecaRequest;
import br.com.lata.velha.ordem_servico.application.dtos.request.CadastrarPecaRequest;
import br.com.lata.velha.ordem_servico.application.dtos.response.PecaResponse;
import br.com.lata.velha.ordem_servico.application.presenters.peca.*;
import br.com.lata.velha.ordem_servico.application.use_cases.peca.*;
import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.application.logging.Logger;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PecaCleanControllerTest {

    @Mock
    private CadastrarPecaGateway cadastrarGateway;
    @Mock
    private CadastrarPecaPresenter cadastrarPresenter;
    @Mock
    private AtualizarPecaGateway atualizarGateway;
    @Mock
    private AtualizarPecaPresenter atualizarPresenter;
    @Mock
    private BuscarPecaPorIdGateway buscarPorIdGateway;
    @Mock
    private BuscarPecaPorIdPresenter buscarPorIdPresenter;
    @Mock
    private BuscarPecasGateway buscarTodosGateway;
    @Mock
    private BuscarPecasPresenter buscarTodosPresenter;
    @Mock
    private DesativarPecaGateway desativarGateway;
    @Mock
    private Logger logger;

    private PecaCleanController controller;

    private final Peca peca = new Peca("1", "Filtro", "Filtro de óleo", new BigDecimal("35.00"));

    @BeforeEach
    void setUp() {
        controller = new PecaCleanController(
                cadastrarGateway, cadastrarPresenter,
                atualizarGateway, atualizarPresenter,
                buscarPorIdGateway, buscarPorIdPresenter,
                buscarTodosGateway, buscarTodosPresenter,
                desativarGateway, logger);
    }

    @Test
    @DisplayName("cadastrar deve persistir a peça e o estoque inicial e chamar o presenter")
    void deveCadastrarPeca() {
        var request = new CadastrarPecaRequest("Filtro", "Filtro de óleo", new BigDecimal("35.00"));
        var response = PecaResponse.from(peca);
        when(cadastrarGateway.salvarPeca(any(Peca.class))).thenReturn(peca);
        when(cadastrarPresenter.present(peca)).thenReturn(response);

        var result = controller.cadastrar(request);

        assertThat(result).isEqualTo(response);
        verify(cadastrarGateway).salvarEstoque(any());
        verify(cadastrarPresenter).present(peca);
    }

    @Test
    @DisplayName("atualizar deve buscar, salvar e apresentar a peça")
    void deveAtualizarPeca() {
        var response = new PecaResponse("1", "Novo", "Nova descrição", BigDecimal.TEN, true);
        when(atualizarGateway.getPecaAtivaPorId("1")).thenReturn(peca);
        when(atualizarGateway.salvarPeca(peca)).thenReturn(peca);
        when(atualizarPresenter.present(peca)).thenReturn(response);

        var result = controller.atualizar("1", new AtualizarPecaRequest("Novo", "Nova descrição", BigDecimal.TEN));

        assertThat(result).isEqualTo(response);
        assertThat(peca.getNome()).isEqualTo("Novo");
    }

    @Test
    @DisplayName("buscarPorId deve delegar para o gateway e presenter")
    void deveBuscarPecaPorId() {
        var response = PecaResponse.from(peca);
        when(buscarPorIdGateway.getPecaAtivaPorId("1")).thenReturn(peca);
        when(buscarPorIdPresenter.present(peca)).thenReturn(response);

        assertThat(controller.buscarPorId("1")).isEqualTo(response);
    }

    @Test
    @DisplayName("buscarTodos deve delegar paginação para o gateway e presenter")
    void deveBuscarTodasPecas() {
        var paginado = new PaginatedResult<>(List.of(peca), 0, 10, 1, 1);
        var apresentado = new PaginatedResult<>(List.of(PecaResponse.from(peca)), 0, 10, 1, 1);
        when(buscarTodosGateway.findAll(0, 10)).thenReturn(paginado);
        when(buscarTodosPresenter.present(paginado)).thenReturn(apresentado);

        var result = controller.buscarTodos(0, 10);

        assertThat(result).isEqualTo(apresentado);
        verify(buscarTodosPresenter).present(paginado);
    }

    @Test
    @DisplayName("desativar deve marcar a peça como inativa e salvar")
    void deveDesativarPeca() {
        when(desativarGateway.getPecaAtivaPorId("1")).thenReturn(peca);
        when(desativarGateway.salvarPeca(peca)).thenReturn(peca);

        controller.desativar("1");

        assertThat(peca.isAtivo()).isFalse();
        verify(desativarGateway).salvarPeca(peca);
    }
}
