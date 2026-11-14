package br.com.lata.velha.ordem_servico.infrastructure.persistence.gateways;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.domain.repositories.PecaRepository;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificarAdminGatewayImplTest {

    private static final String PECA_ID = "10";

    @Mock
    private PecaRepository pecaRepository;

    @Mock
    private Logger logger;

    private NotificarAdminGatewayImpl gateway;

    @BeforeEach
    void setUp() {
        gateway = new NotificarAdminGatewayImpl(pecaRepository, logger);
    }

    @Test
    @DisplayName("findPecaPorId deve retornar a peça ativa")
    void deveBuscarPecaAtiva() {
        var peca = new Peca(PECA_ID, "Filtro", "Filtro de óleo", new BigDecimal("35.00"));
        when(pecaRepository.getActiveById(PECA_ID)).thenReturn(peca);

        assertThat(gateway.findPecaPorId(PECA_ID)).contains(peca);
    }

    @Test
    @DisplayName("findPecaPorId deve retornar vazio quando o repositório lança exceção")
    void deveRetornarVazioQuandoRepositorioFalha() {
        when(pecaRepository.getActiveById(PECA_ID)).thenThrow(new RuntimeException("mongo down"));

        assertThat(gateway.findPecaPorId(PECA_ID)).isEmpty();
        verify(logger).logError(anyString(), any(Exception.class));
    }

    @Test
    @DisplayName("findPecaPorId deve retornar vazio quando o repositório retorna nulo")
    void deveRetornarVazioQuandoRepositorioRetornaNulo() {
        when(pecaRepository.getActiveById(PECA_ID)).thenReturn(null);

        assertThat(gateway.findPecaPorId(PECA_ID)).isEmpty();
    }

    @Test
    @DisplayName("getAdminEmails deve separar, aparar e ignorar entradas vazias")
    void deveSepararEmailsDeAdmin() {
        ReflectionTestUtils.setField(gateway, "adminEmailsConfig", " admin1@lata.com, ,admin2@lata.com ");

        assertThat(gateway.getAdminEmails()).containsExactly("admin1@lata.com", "admin2@lata.com");
    }

    @Test
    @DisplayName("getAdminEmails deve retornar vazio quando a config está em branco")
    void deveRetornarVazioQuandoConfigEmBranco() {
        ReflectionTestUtils.setField(gateway, "adminEmailsConfig", "   ");

        assertThat(gateway.getAdminEmails()).isEmpty();
    }

    @Test
    @DisplayName("getAdminEmails deve retornar vazio quando a config é nula")
    void deveRetornarVazioQuandoConfigNula() {
        ReflectionTestUtils.setField(gateway, "adminEmailsConfig", null);

        assertThat(gateway.getAdminEmails()).isEqualTo(List.of());
    }
}
