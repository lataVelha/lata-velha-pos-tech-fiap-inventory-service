package br.com.lata.velha.ordem_servico.domain.entities;

import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PecaAlocadaTest {

    private PecaAlocada buildPeca(int solicitada, int reservada, int encomendada, StatusPecaAlocada status) {
        return new PecaAlocada(1L, 10L, 99L, new BigDecimal("10.00"), solicitada, reservada, encomendada, 0, status, LocalDateTime.now());
    }

    @Test
    void deveCriarPecaAlocadaComDadosValidos() {
        PecaAlocada peca = buildPeca(5, 0, 0, StatusPecaAlocada.PENDENTE);

        assertThat(peca.getId()).isEqualTo(1L);
        assertThat(peca.getPecaId()).isEqualTo(10L);
        assertThat(peca.getExecucaoServicoId()).isEqualTo(99L);
        assertThat(peca.getQuantidadeSolicitada()).isEqualTo(5);
        assertThat(peca.getStatus()).isEqualTo(StatusPecaAlocada.PENDENTE);
    }

    @Test
    void deveCriarPecaAlocadaViaFactoryMethod() {
        PecaAlocada peca = PecaAlocada.create(10L, 99L, BigDecimal.ZERO, 3);

        assertThat(peca.getId()).isNull();
        assertThat(peca.getPecaId()).isEqualTo(10L);
        assertThat(peca.getExecucaoServicoId()).isEqualTo(99L);
        assertThat(peca.getQuantidadeSolicitada()).isEqualTo(3);
        assertThat(peca.getQuantidadeReservada()).isZero();
        assertThat(peca.getQuantidadeEncomendada()).isZero();
        assertThat(peca.getStatus()).isEqualTo(StatusPecaAlocada.PENDENTE);
    }

    @Test
    void deveFalharQuandoPecaIdForNulo() {
        var now = LocalDateTime.now();
        assertThatThrownBy(() ->
                new PecaAlocada(1L, null, 99L, BigDecimal.ZERO, 5, 0, 0, 0, StatusPecaAlocada.PENDENTE, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Peça obrigatória");
    }

    @Test
    void deveFalharQuandoExecucaoServicoIdForNulo() {
        var now = LocalDateTime.now();
        assertThatThrownBy(() ->
                new PecaAlocada(1L, 10L, null, BigDecimal.ZERO, 5, 0, 0, 0, StatusPecaAlocada.PENDENTE, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Serviço obrigatório");
    }

    @Test
    void deveFalharQuandoQuantidadeSolicitadaForInvalida() {
        var now = LocalDateTime.now();
        assertThatThrownBy(() ->
                new PecaAlocada(1L, 10L, 99L, BigDecimal.ZERO, 0, 0, 0, 0, StatusPecaAlocada.PENDENTE, now))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade inválida");
    }

    @Test
    void deveReservarTotalmenteQuandoEstoqueEhSuficiente() {
        PecaAlocada peca = buildPeca(3, 0, 0, StatusPecaAlocada.PENDENTE);
        PecaEstoque estoque = new PecaEstoque(10L, 10, 10);

        peca.reservar(estoque);

        assertThat(peca.getQuantidadeReservada()).isEqualTo(3);
        assertThat(peca.getQuantidadeEncomendada()).isZero();
        assertThat(peca.getStatus()).isEqualTo(StatusPecaAlocada.RESERVADA);
        assertThat(estoque.getQuantidadeDisponivel()).isEqualTo(7);
    }

    @Test
    void deveReservarParcialmenteQuandoEstoqueEhInsuficiente() {
        PecaAlocada peca = buildPeca(5, 0, 0, StatusPecaAlocada.PENDENTE);
        PecaEstoque estoque = new PecaEstoque(10L, 3, 3);

        peca.reservar(estoque);

        assertThat(peca.getQuantidadeReservada()).isEqualTo(3);
        assertThat(peca.getQuantidadeEncomendada()).isEqualTo(2);
        assertThat(peca.getStatus()).isEqualTo(StatusPecaAlocada.PARCIAL);
        assertThat(estoque.getQuantidadeDisponivel()).isZero();
    }

    @Test
    void deveEncomendarTudoQuandoEstoqueEhNulo() {
        PecaAlocada peca = buildPeca(4, 0, 0, StatusPecaAlocada.PENDENTE);

        peca.reservar(null);

        assertThat(peca.getQuantidadeReservada()).isZero();
        assertThat(peca.getQuantidadeEncomendada()).isEqualTo(4);
        assertThat(peca.getStatus()).isEqualTo(StatusPecaAlocada.ENCOMENDA);
    }

    @Test
    void deveEncomendarTudoQuandoEstoqueZero() {
        PecaAlocada peca = buildPeca(4, 0, 0, StatusPecaAlocada.PENDENTE);
        PecaEstoque estoque = new PecaEstoque(10L, 0, 0);

        peca.reservar(estoque);

        assertThat(peca.getQuantidadeReservada()).isZero();
        assertThat(peca.getQuantidadeEncomendada()).isEqualTo(4);
        assertThat(peca.getStatus()).isEqualTo(StatusPecaAlocada.ENCOMENDA);
    }

    @Test
    void deveRegistrarInstalacao() {
        PecaAlocada peca = buildPeca(3, 3, 0, StatusPecaAlocada.RESERVADA);

        peca.instalar(3);

        assertThat(peca.getQuantidadeInstalada()).isEqualTo(3);
        assertThat(peca.getStatus()).isEqualTo(StatusPecaAlocada.INSTALADA);
    }

    @Test
    void deveRegistrarInstalacaoParcial() {
        PecaAlocada peca = buildPeca(5, 5, 0, StatusPecaAlocada.RESERVADA);

        peca.instalar(2);

        assertThat(peca.getQuantidadeInstalada()).isEqualTo(2);
        assertThat(peca.getStatus()).isEqualTo(StatusPecaAlocada.RESERVADA);
    }

    @Test
    void deveFalharInstalacaoQuandoQuantidadeInvalida() {
        PecaAlocada peca = buildPeca(3, 3, 0, StatusPecaAlocada.RESERVADA);

        assertThatThrownBy(() -> peca.instalar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade inválida");

        assertThatThrownBy(() -> peca.instalar(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade inválida");
    }

    @Test
    void deveFalharInstalacaoQuandoReservadaInsuficiente() {
        PecaAlocada peca = buildPeca(5, 2, 0, StatusPecaAlocada.RESERVADA);

        assertThatThrownBy(() -> peca.instalar(3))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Quantidade reservada insuficiente para instalar");
    }

    @Test
    void deveRetornarIsReservadaQuandoCompleto() {
        PecaAlocada peca = buildPeca(3, 3, 0, StatusPecaAlocada.RESERVADA);

        assertThat(peca.isReservada()).isTrue();
    }

    @Test
    void deveRetornarFalsoParaIsReservadaQuandoParcial() {
        PecaAlocada peca = buildPeca(5, 3, 2, StatusPecaAlocada.PARCIAL);

        assertThat(peca.isReservada()).isFalse();
    }

    @Test
    void deveImplementarEqualsEHashCodePeloId() {
        PecaAlocada a = buildPeca(3, 0, 0, StatusPecaAlocada.PENDENTE);
        PecaAlocada b = new PecaAlocada(1L, 20L, 88L, BigDecimal.ZERO, 2, 0, 0, 0, StatusPecaAlocada.RESERVADA, LocalDateTime.now());
        PecaAlocada c = new PecaAlocada(2L, 10L, 99L, BigDecimal.ZERO, 3, 0, 0, 0, StatusPecaAlocada.PENDENTE, LocalDateTime.now());

        assertThat(a)
                .isEqualTo(b)
                .isNotEqualTo(c)
                .hasSameHashCodeAs(b);
    }

    @Test
    void naodeveEqualQuandoComparadoComOutroTipo() {
        PecaAlocada peca = buildPeca(3, 0, 0, StatusPecaAlocada.PENDENTE);

        assertThat(peca).isNotEqualTo("string");
    }

    @Test
    void deveRetornarAtualizado() {
        PecaAlocada peca = buildPeca(3, 0, 0, StatusPecaAlocada.PENDENTE);

        assertThat(peca.getAtualizado()).isNotNull();
    }
}
