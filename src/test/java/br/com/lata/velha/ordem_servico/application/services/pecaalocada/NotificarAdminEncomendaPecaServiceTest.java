package br.com.lata.velha.ordem_servico.application.services.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.application.gateways.EmailProvider;
import br.com.lata.velha.shared.application.gateways.EmailTemplateProvider;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificarAdminEncomendaPecaServiceTest {

    private static final String PECA_ID = "10";
    private static final Long EXECUCAO_ID = 7L;

    @Mock
    private NotificarAdminEncomendaPecaGateway gateway;

    @Mock
    private EmailProvider emailProvider;

    @Mock
    private EmailTemplateProvider templateProvider;

    @Mock
    private Logger logger;

    private NotificarAdminEncomendaPecaService service;

    private final NotificarAdminEncomendaPecaService.Input input =
            new NotificarAdminEncomendaPecaService.Input(EXECUCAO_ID, PECA_ID, 3);

    private final Peca peca = new Peca(PECA_ID, "Filtro de óleo", "Filtro para motor", new BigDecimal("35.00"));

    @BeforeEach
    void setUp() {
        service = new NotificarAdminEncomendaPecaService(gateway, emailProvider, templateProvider, logger);
    }

    @Test
    @DisplayName("deve enviar e-mail para cada admin com as variáveis do template")
    void deveEnviarEmailParaCadaAdmin() {
        when(gateway.findPecaPorId(PECA_ID)).thenReturn(Optional.of(peca));
        when(gateway.getAdminEmails()).thenReturn(List.of("admin1@lata.com", "admin2@lata.com"));
        when(templateProvider.render(eq("peca-encomenda"), anyMap())).thenReturn("<html/>");

        service.execute(input);

        verify(emailProvider).send(eq("admin1@lata.com"), eq("Encomenda de Peça — Execução Nº 7"), eq("<html/>"));
        verify(emailProvider).send(eq("admin2@lata.com"), eq("Encomenda de Peça — Execução Nº 7"), eq("<html/>"));

        @SuppressWarnings("unchecked")
        var captor = ArgumentCaptor.forClass(Map.class);
        verify(templateProvider, times(2)).render(eq("peca-encomenda"), captor.capture());
        assertThat(captor.getAllValues().getFirst())
                .containsEntry("execucaoServicoId", EXECUCAO_ID)
                .containsEntry("pecaNome", "Filtro de óleo")
                .containsEntry("quantidade", 3);
    }

    @Test
    @DisplayName("não deve enviar nada quando a peça não existe")
    void naoDeveEnviarQuandoPecaNaoExiste() {
        when(gateway.findPecaPorId(PECA_ID)).thenReturn(Optional.empty());

        service.execute(input);

        verifyNoInteractions(emailProvider, templateProvider);
    }

    @Test
    @DisplayName("não deve propagar erro ao buscar a peça")
    void naoDevePropagarErroAoBuscarPeca() {
        when(gateway.findPecaPorId(PECA_ID)).thenThrow(new RuntimeException("mongo down"));

        service.execute(input);

        verifyNoInteractions(emailProvider, templateProvider);
        verify(logger).logError(anyString(), any(Exception.class));
    }

    @Test
    @DisplayName("não deve enviar nada quando não há admins configurados")
    void naoDeveEnviarQuandoSemAdmins() {
        when(gateway.findPecaPorId(PECA_ID)).thenReturn(Optional.of(peca));
        when(gateway.getAdminEmails()).thenReturn(List.of());

        service.execute(input);

        verifyNoInteractions(emailProvider, templateProvider);
    }

    @Test
    @DisplayName("deve continuar para os demais admins quando um envio falha")
    void deveContinuarQuandoEnvioFalha() {
        when(gateway.findPecaPorId(PECA_ID)).thenReturn(Optional.of(peca));
        when(gateway.getAdminEmails()).thenReturn(List.of("admin1@lata.com", "admin2@lata.com"));
        when(templateProvider.render(anyString(), anyMap())).thenReturn("<html/>");
        doThrow(new RuntimeException("smtp fora"))
                .when(emailProvider).send(eq("admin1@lata.com"), anyString(), anyString());

        service.execute(input);

        verify(emailProvider).send(eq("admin2@lata.com"), anyString(), anyString());
        verify(logger).logError(anyString(), any(Exception.class));
    }
}
