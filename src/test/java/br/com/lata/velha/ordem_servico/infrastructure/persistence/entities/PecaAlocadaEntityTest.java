package br.com.lata.velha.ordem_servico.infrastructure.persistence.entities;

import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PecaAlocadaEntityTest {

    @Test
    void deveInicializarComQuantidadesPadrao() {
        PecaAlocadaEntity entity = new PecaAlocadaEntity();

        assertThat(entity.getQuantidadeReservada()).isZero();
        assertThat(entity.getQuantidadeEncomendada()).isZero();
    }

    @Test
    void deveSetarEObterCampos() {
        PecaAlocadaEntity entity = new PecaAlocadaEntity();
        PecaEntity peca = new PecaEntity();
        LocalDateTime atualizado = LocalDateTime.now();

        peca.setId(3L);

        entity.setId(1L);
        entity.setQuantidadeSolicitada(8);
        entity.setQuantidadeReservada(5);
        entity.setQuantidadeEncomendada(3);
        entity.setStatus(StatusPecaAlocada.PARCIAL);
        entity.setAtualizado(atualizado);
        entity.setExecucaoServicoId(2L);
        entity.setPeca(peca);

        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getQuantidadeSolicitada()).isEqualTo(8);
        assertThat(entity.getQuantidadeReservada()).isEqualTo(5);
        assertThat(entity.getQuantidadeEncomendada()).isEqualTo(3);
        assertThat(entity.getStatus()).isEqualTo(StatusPecaAlocada.PARCIAL);
        assertThat(entity.getAtualizado()).isEqualTo(atualizado);
        assertThat(entity.getExecucaoServicoId()).isEqualTo(2L);
        assertThat(entity.getPeca().getId()).isEqualTo(3L);
    }
}
