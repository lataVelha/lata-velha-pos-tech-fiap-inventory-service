package br.com.lata.velha.ordem_servico.application.use_cases.peca;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DesativarPecaUseCaseTest {

    @Mock
    private DesativarPecaGateway gateway;

    @Mock
    private Logger logger;

    @Test
    @DisplayName("Deve desativar peça ativa com sucesso")
    void deveDesativarPecaAtivaComSucesso() {
        var peca = new Peca(1L, "Filtro", "Filtro de óleo", new BigDecimal("35.00"), true);
        when(gateway.getPecaAtivaPorId(1L)).thenReturn(peca);

        DesativarPecaUseCase useCase = new DesativarPecaUseCase(gateway, logger);
        useCase.execute(1L);

        assertFalse(peca.isAtivo());
        verify(gateway).salvarPeca(peca);
    }

    @Test
    @DisplayName("Deve falhar quando peça já estiver desativada")
    void deveFalharQuandoPecaJaEstiverDesativada() {
        var peca = new Peca(1L, "Filtro", "Filtro de óleo", new BigDecimal("35.00"), false);
        when(gateway.getPecaAtivaPorId(1L)).thenReturn(peca);

        DesativarPecaUseCase useCase = new DesativarPecaUseCase(gateway, logger);

        assertThrows(IllegalArgumentException.class, () -> useCase.execute(1L));
        verify(gateway, never()).salvarPeca(peca);
    }
}
