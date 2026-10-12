package br.com.lata.velha.ordem_servico.infrastructure.persistence.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PecaEstoqueEntityTest {

    @Test
    void deveSetarEObterCampos() {
        PecaEstoqueEntity entity = new PecaEstoqueEntity();

        entity.setPecaId(5L);
        entity.setQuantidadeArmazenada(12);

        assertThat(entity.getPecaId()).isEqualTo(5L);
        assertThat(entity.getQuantidadeArmazenada()).isEqualTo(12);
        assertThat(entity.toString()).contains("quantidadeArmazenada=12");
    }

    @Test
    void deveAplicarEqualsEHashCodeComMesmoConteudo() {
        PecaEstoqueEntity a = new PecaEstoqueEntity();
        a.setPecaId(1L);
        a.setQuantidadeArmazenada(3);

        PecaEstoqueEntity b = new PecaEstoqueEntity();
        b.setPecaId(1L);
        b.setQuantidadeArmazenada(3);

        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
    }
}
