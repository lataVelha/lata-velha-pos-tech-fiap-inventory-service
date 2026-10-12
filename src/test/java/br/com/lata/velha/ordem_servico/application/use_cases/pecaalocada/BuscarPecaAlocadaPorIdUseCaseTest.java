package br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarPecaAlocadaPorIdUseCaseTest {

    @Mock
    private BuscarPecaAlocadaPorIdGateway gateway;

    @Mock
    private Logger logger;

    @Test
    void deveBuscarPecaAlocadaComSucesso() {
        PecaAlocada pecaAlocada = new PecaAlocada(1L, 2L, 99L, BigDecimal.ZERO, 2, 0, 0, 0, StatusPecaAlocada.PENDENTE, LocalDateTime.now());

        when(gateway.getPecaAlocadaPorId(1L)).thenReturn(pecaAlocada);

        BuscarPecaAlocadaPorIdUseCase useCase = new BuscarPecaAlocadaPorIdUseCase(gateway, logger);
        PecaAlocada response = useCase.execute(1L);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getPecaId()).isEqualTo(2L);
        verify(gateway).getPecaAlocadaPorId(1L);
    }
}
