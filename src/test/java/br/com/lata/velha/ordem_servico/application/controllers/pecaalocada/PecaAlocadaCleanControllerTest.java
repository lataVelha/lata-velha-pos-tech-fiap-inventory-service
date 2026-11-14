package br.com.lata.velha.ordem_servico.application.controllers.pecaalocada;

import br.com.lata.velha.ordem_servico.application.dtos.response.PecaAlocadaResponse;
import br.com.lata.velha.ordem_servico.application.presenters.pecaalocada.BuscarPecaAlocadaPorIdPresenter;
import br.com.lata.velha.ordem_servico.application.presenters.pecaalocada.BuscarPecasAlocadasPresenter;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.BuscarPecaAlocadaPorIdGateway;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.BuscarPecasAlocadasGateway;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import br.com.lata.velha.shared.application.logging.Logger;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PecaAlocadaCleanControllerTest {

    @Mock
    private BuscarPecaAlocadaPorIdGateway buscarPorIdGateway;
    @Mock
    private BuscarPecaAlocadaPorIdPresenter buscarPorIdPresenter;
    @Mock
    private BuscarPecasAlocadasGateway buscarTodosGateway;
    @Mock
    private BuscarPecasAlocadasPresenter buscarTodosPresenter;
    @Mock
    private Logger logger;

    private PecaAlocadaCleanController controller;

    private final PecaAlocada alocada = new PecaAlocada("1", "10", 99L, new BigDecimal("35.00"),
            2, 2, 0, 0, StatusPecaAlocada.RESERVADA, LocalDateTime.now());

    @BeforeEach
    void setUp() {
        controller = new PecaAlocadaCleanController(
                buscarPorIdGateway, buscarPorIdPresenter,
                buscarTodosGateway, buscarTodosPresenter,
                logger);
    }

    @Test
    @DisplayName("buscarPorId deve delegar para o use case e presenter")
    void deveBuscarPecaAlocadaPorId() {
        var response = PecaAlocadaResponse.from(alocada);
        when(buscarPorIdGateway.getPecaAlocadaPorId("1")).thenReturn(alocada);
        when(buscarPorIdPresenter.present(alocada)).thenReturn(response);

        var result = controller.buscarPorId("1");

        assertThat(result).isEqualTo(response);
        verify(buscarPorIdGateway).getPecaAlocadaPorId("1");
    }

    @Test
    @DisplayName("buscarTodos deve delegar paginação para o use case e presenter")
    void deveBuscarPecasAlocadas() {
        var paginado = new PaginatedResult<>(List.of(alocada), 0, 10, 1, 1);
        var apresentado = new PaginatedResult<>(List.of(PecaAlocadaResponse.from(alocada)), 0, 10, 1, 1);
        when(buscarTodosGateway.findByExecucaoServicoId(99L, 0, 10)).thenReturn(paginado);
        when(buscarTodosPresenter.present(paginado)).thenReturn(apresentado);

        var result = controller.buscarTodos(99L, 0, 10);

        assertThat(result).isEqualTo(apresentado);
        verify(buscarTodosGateway).findByExecucaoServicoId(99L, 0, 10);
    }
}
