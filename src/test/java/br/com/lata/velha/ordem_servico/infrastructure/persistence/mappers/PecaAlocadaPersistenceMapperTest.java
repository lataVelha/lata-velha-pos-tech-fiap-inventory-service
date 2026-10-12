package br.com.lata.velha.ordem_servico.infrastructure.persistence.mappers;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaAlocadaEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class PecaAlocadaPersistenceMapperTest {

    private PecaAlocadaPersistenceMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new PecaAlocadaPersistenceMapper();
    }

    // ------------------------------------------------------------------ toDomain

    @Test
    @DisplayName("toDomain deve retornar null quando entity é null")
    void toDomain_deveRetornarNullQuandoEntityEhNull() {
        assertThat(mapper.toDomain(null)).isNull();
    }

    @Test
    @DisplayName("toDomain deve mapear entity para domain corretamente")
    void toDomain_deveMapearEntityParaDomainCorretamente() {
        var now = LocalDateTime.now();
        var entity = new PecaAlocadaEntity(
                10L, 5L, 1L, new BigDecimal("49.90"),
                4, 2, 2, 0,
                StatusPecaAlocada.PARCIAL, now
        );

        var domain = mapper.toDomain(entity);

        assertThat(domain).isNotNull();
        assertThat(domain.getId()).isEqualTo(10L);
        assertThat(domain.getPecaId()).isEqualTo(5L);
        assertThat(domain.getExecucaoServicoId()).isEqualTo(1L);
        assertThat(domain.getValorUnitarioPeca()).isEqualByComparingTo("49.90");
        assertThat(domain.getQuantidadeSolicitada()).isEqualTo(4);
        assertThat(domain.getQuantidadeReservada()).isEqualTo(2);
        assertThat(domain.getQuantidadeEncomendada()).isEqualTo(2);
        assertThat(domain.getStatus()).isEqualTo(StatusPecaAlocada.PARCIAL);
        assertThat(domain.getAtualizado()).isEqualTo(now);
    }

    @Test
    @DisplayName("toDomain deve mapear status INSTALADA corretamente")
    void toDomain_deveMapearStatusInstalada() {
        var entity = new PecaAlocadaEntity(
                20L, 3L, 2L, new BigDecimal("30.00"),
                2, 2, 0, 2,
                StatusPecaAlocada.INSTALADA, null
        );

        var domain = mapper.toDomain(entity);

        assertThat(domain.getStatus()).isEqualTo(StatusPecaAlocada.INSTALADA);
        assertThat(domain.getQuantidadeInstalada()).isEqualTo(2);
    }

    // ------------------------------------------------------------------ toEntity

    @Test
    @DisplayName("toEntity deve retornar null quando domain é null")
    void toEntity_deveRetornarNullQuandoDomainEhNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }

    @Test
    @DisplayName("toEntity deve mapear domain para entity corretamente")
    void toEntity_deveMapearDomainParaEntityCorretamente() {
        var now = LocalDateTime.now();
        var domain = new PecaAlocada(
                10L, 5L, 1L, new BigDecimal("49.90"),
                4, 2, 2, 0,
                StatusPecaAlocada.PARCIAL, now
        );

        var entity = mapper.toEntity(domain);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(10L);
        assertThat(entity.getPecaId()).isEqualTo(5L);
        assertThat(entity.getExecucaoServicoId()).isEqualTo(1L);
        assertThat(entity.getValorUnitario()).isEqualByComparingTo("49.90");
        assertThat(entity.getQuantidadeSolicitada()).isEqualTo(4);
        assertThat(entity.getQuantidadeReservada()).isEqualTo(2);
        assertThat(entity.getQuantidadeEncomendada()).isEqualTo(2);
        assertThat(entity.getStatus()).isEqualTo(StatusPecaAlocada.PARCIAL);
    }

    @Test
    @DisplayName("roundTrip domain→entity→domain deve preservar dados")
    void roundTrip_devePreservarDados() {
        var original = new PecaAlocada(
                15L, 7L, 3L, new BigDecimal("25.00"),
                3, 1, 2, 0,
                StatusPecaAlocada.ENCOMENDA, LocalDateTime.now()
        );

        var roundTrip = mapper.toDomain(mapper.toEntity(original));

        assertThat(roundTrip.getId()).isEqualTo(original.getId());
        assertThat(roundTrip.getPecaId()).isEqualTo(original.getPecaId());
        assertThat(roundTrip.getStatus()).isEqualTo(original.getStatus());
        assertThat(roundTrip.getValorUnitarioPeca()).isEqualByComparingTo(original.getValorUnitarioPeca());
    }
}
