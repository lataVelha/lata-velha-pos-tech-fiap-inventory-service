package br.com.lata.velha.ordem_servico.infrastructure.persistence.entities;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class PecaEntityTest {

    @Test
    void deveInicializarComAtivoTruePorPadrao() {
        PecaEntity entity = new PecaEntity();

        assertThat(entity.isAtivo()).isTrue();
    }

    @Test
    void deveSetarEObterCampos() {
        PecaEntity entity = new PecaEntity();

        entity.setId(7L);
        entity.setNome("Pastilha");
        entity.setDescricao("Pastilha dianteira");
        entity.setValor(new BigDecimal("199.90"));
        entity.setAtivo(false);

        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getNome()).isEqualTo("Pastilha");
        assertThat(entity.getDescricao()).isEqualTo("Pastilha dianteira");
        assertThat(entity.getValor()).isEqualByComparingTo("199.90");
        assertThat(entity.isAtivo()).isFalse();
    }
}
