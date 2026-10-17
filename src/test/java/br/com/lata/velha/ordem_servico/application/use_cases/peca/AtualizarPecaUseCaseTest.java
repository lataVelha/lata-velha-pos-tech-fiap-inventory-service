package br.com.lata.velha.ordem_servico.application.use_cases.peca;

import br.com.lata.velha.ordem_servico.application.dtos.request.AtualizarPecaRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AtualizarPecaUseCaseTest {

    @Mock
    private AtualizarPecaGateway gateway;

    @Mock
    private Logger logger;

    @InjectMocks
    private AtualizarPecaUseCase useCase;

    @Test
    @DisplayName("Deve atualizar peca com sucesso")
    void deveAtualizarPecaComSucesso() {
        var request = new AtualizarPecaRequest("Pastilha premium", "Pastilha ceramica", new BigDecimal("180.00"));
        var peca = new Peca(1L, "Pastilha", "Pastilha comum", new BigDecimal("130.00"), true);

        when(gateway.getPecaAtivaPorId(1L)).thenReturn(peca);
        when(gateway.salvarPeca(peca)).thenReturn(peca);

        var result = useCase.execute(1L, request);

        assertThat(peca.getNome()).isEqualTo("Pastilha premium");
        assertThat(peca.getDescricao()).isEqualTo("Pastilha ceramica");
        assertThat(peca.getValor()).isEqualByComparingTo("180.00");
        assertThat(result.getNome()).isEqualTo("Pastilha premium");
        verify(gateway).salvarPeca(peca);
    }

    @Test
    @DisplayName("Deve falhar quando peca nao existir")
    void deveFalharQuandoPecaNaoExistir() {
        var request = new AtualizarPecaRequest("Nome", "Descricao", new BigDecimal("50.00"));
        when(gateway.getPecaAtivaPorId(99L)).thenThrow(new IllegalArgumentException("Peca nao encontrada"));

        assertThatThrownBy(() -> useCase.execute(99L, request))
                .isInstanceOf(IllegalArgumentException.class);
        verify(gateway, never()).salvarPeca(any());
    }
}
