package br.com.lata.velha.ordem_servico.application.controllers.pecaestoque;

import br.com.lata.velha.ordem_servico.application.dtos.request.AjustarPecaEstoqueRequest;
import br.com.lata.velha.ordem_servico.application.dtos.request.MovimentarPecaEstoqueRequest;
import br.com.lata.velha.ordem_servico.application.dtos.response.PecaEstoqueResponse;
import br.com.lata.velha.ordem_servico.application.presenters.pecaestoque.*;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque.*;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.shared.application.gateways.EventPublisher;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PecaEstoqueCleanControllerTest {

    @Mock
    private BuscarPecaEstoqueGateway buscarGateway;
    @Mock
    private BuscarPecaEstoquePresenter buscarPresenter;
    @Mock
    private EntradaPecaEstoqueGateway entradaGateway;
    @Mock
    private EntradaPecaEstoquePresenter entradaPresenter;
    @Mock
    private SaidaPecaEstoqueGateway saidaGateway;
    @Mock
    private SaidaPecaEstoquePresenter saidaPresenter;
    @Mock
    private AjustarPecaEstoqueGateway ajustarGateway;
    @Mock
    private AjustarPecaEstoquePresenter ajustarPresenter;
    @Mock
    private EventPublisher eventPublisher;
    @Mock
    private Logger logger;

    private PecaEstoqueCleanController controller;

    @BeforeEach
    void setUp() {
        controller = new PecaEstoqueCleanController(
                buscarGateway, buscarPresenter,
                entradaGateway, entradaPresenter,
                saidaGateway, saidaPresenter,
                ajustarGateway, ajustarPresenter,
                eventPublisher, logger);
    }

    @Test
    @DisplayName("buscar deve delegar para o caso de uso e presenter")
    void deveBuscarEstoque() {
        var estoque = new PecaEstoque("1", 10, 8);
        var response = PecaEstoqueResponse.from(estoque);
        when(buscarGateway.getEstoquePorPecaId("1")).thenReturn(estoque);
        when(buscarPresenter.present(estoque)).thenReturn(response);

        assertThat(controller.buscar("1")).isEqualTo(response);
        verify(buscarGateway).getPecaAtivaPorId("1");
    }

    @Test
    @DisplayName("entrada deve delegar para o caso de uso e presenter")
    void deveRegistrarEntrada() {
        var estoque = new PecaEstoque("1", 10, 10);
        var response = new PecaEstoqueResponse("1", 15, 15);
        when(entradaGateway.getEstoquePorPecaId("1")).thenReturn(estoque);
        when(entradaGateway.buscarPendentesPorPeca("1")).thenReturn(List.of());
        when(entradaGateway.salvarEstoque(estoque)).thenReturn(new PecaEstoque("1", 15, 15));
        when(entradaPresenter.present(any())).thenReturn(response);

        var result = controller.entrada("1", new MovimentarPecaEstoqueRequest(5));

        assertThat(result).isEqualTo(response);
        verify(entradaPresenter).present(any());
    }

    @Test
    @DisplayName("saida deve delegar para o caso de uso e presenter")
    void deveRegistrarSaida() {
        var estoque = new PecaEstoque("1", 10, 10);
        var response = new PecaEstoqueResponse("1", 5, 5);
        when(saidaGateway.getEstoquePorPecaId("1")).thenReturn(estoque);
        when(saidaGateway.salvarEstoque(estoque)).thenReturn(new PecaEstoque("1", 5, 5));
        when(saidaPresenter.present(any())).thenReturn(response);

        var result = controller.saida("1", new MovimentarPecaEstoqueRequest(5));

        assertThat(result).isEqualTo(response);
        verify(saidaPresenter).present(any());
    }

    @Test
    @DisplayName("ajustar deve delegar para o caso de uso e presenter")
    void deveAjustarEstoque() {
        var estoque = new PecaEstoque("1", 10, 10);
        var response = new PecaEstoqueResponse("1", 100, 100);
        when(ajustarGateway.getEstoquePorPecaId("1")).thenReturn(estoque);
        when(ajustarGateway.salvarEstoque(estoque)).thenReturn(new PecaEstoque("1", 100, 100));
        when(ajustarPresenter.present(any())).thenReturn(response);

        var result = controller.ajustar("1", new AjustarPecaEstoqueRequest(100, 100));

        assertThat(result).isEqualTo(response);
        verify(ajustarPresenter).present(any());
    }
}
