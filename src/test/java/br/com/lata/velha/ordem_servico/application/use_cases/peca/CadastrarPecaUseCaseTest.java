package br.com.lata.velha.ordem_servico.application.use_cases.peca;

import br.com.lata.velha.ordem_servico.application.dtos.request.CadastrarPecaRequest;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CadastrarPecaUseCaseTest {

    @Mock
    private CadastrarPecaGateway gateway;

    @Mock
    private Logger logger;

    @InjectMocks
    private CadastrarPecaUseCase useCase;

    @Test
    @DisplayName("Deve cadastrar peca com sucesso")
    void deveCadastrarPecaComSucesso() {
        var request = new CadastrarPecaRequest("Pastilha", "Pastilha dianteira", new BigDecimal("150.00"));
        var savedDomain = new Peca(10L, "Pastilha", "Pastilha dianteira", new BigDecimal("150.00"), true);

        when(gateway.salvarPeca(any())).thenReturn(savedDomain);

        var result = useCase.execute(request);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getNome()).isEqualTo("Pastilha");
        assertThat(result.isAtivo()).isTrue();
        verify(gateway).salvarPeca(any());
        verify(gateway).salvarEstoque(any());
    }
}
