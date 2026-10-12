package br.com.lata.velha.ordem_servico.application.use_cases.peca;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BuscarPecaPorIdUseCaseTest {

    @Mock
    private BuscarPecaPorIdGateway gateway;

    @Mock
    private Logger logger;

    @InjectMocks
    private BuscarPecaPorIdUseCase useCase;

    @Test
    @DisplayName("Deve buscar peca ativa por ID com sucesso")
    void deveBuscarPecaAtivaPorIdComSucesso() {
        var peca = new Peca(1L, "Disco de freio", "Disco dianteiro", new BigDecimal("220.00"), true);
        when(gateway.getPecaAtivaPorId(1L)).thenReturn(peca);

        var result = useCase.execute(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getNome()).isEqualTo("Disco de freio");
        verify(gateway).getPecaAtivaPorId(1L);
    }

    @Test
    @DisplayName("Deve falhar ao buscar peca inexistente")
    void deveFalharAoBuscarPecaInexistente() {
        when(gateway.getPecaAtivaPorId(99L)).thenThrow(new IllegalArgumentException("Peca nao encontrada"));

        assertThatThrownBy(() -> useCase.execute(99L))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
