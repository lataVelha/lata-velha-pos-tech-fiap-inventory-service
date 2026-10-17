package br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import br.com.lata.velha.shared.application.logging.Logger;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
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
class BuscarPecasAlocadasUseCaseTest {

    @Mock
    private BuscarPecasAlocadasGateway gateway;

    @Mock
    private Logger logger;

    @Test
    void deveBuscarPecasDeUmServicoComSucesso() {
        PecaAlocada peca1 = new PecaAlocada(1L, 2L, 99L, BigDecimal.ZERO, 2, 0, 0, 0, StatusPecaAlocada.PENDENTE, LocalDateTime.now());
        PecaAlocada peca2 = new PecaAlocada(2L, 2L, 99L, BigDecimal.ZERO, 4, 0, 0, 0, StatusPecaAlocada.PENDENTE, LocalDateTime.now());

        PaginatedResult<PecaAlocada> paginatedResult = new PaginatedResult<>(
                List.of(peca1, peca2), 0, 10, 2L, 1
        );

        when(gateway.findByExecucaoServicoId(99L, 0, 10)).thenReturn(paginatedResult);

        BuscarPecasAlocadasUseCase useCase = new BuscarPecasAlocadasUseCase(gateway, logger);
        PaginatedResult<PecaAlocada> response = useCase.execute(99L, 0, 10);

        assertThat(response).isNotNull();
        assertThat(response.content()).hasSize(2);
        assertThat(response.content().get(0).getId()).isEqualTo(1L);
        assertThat(response.content().get(1).getId()).isEqualTo(2L);
        assertThat(response.content().get(0).getExecucaoServicoId()).isEqualTo(99L);
        verify(gateway).findByExecucaoServicoId(99L, 0, 10);
    }
}
