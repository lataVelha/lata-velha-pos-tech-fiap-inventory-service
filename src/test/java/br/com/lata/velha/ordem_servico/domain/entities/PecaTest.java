package br.com.lata.velha.ordem_servico.domain.entities;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PecaTest {

    @Test
    @DisplayName("deve criar peça ativa por padrão no construtor com 4 argumentos")
    void shouldCreateActiveByDefaultWithFourArgsConstructor() {
        Peca peca = new Peca(1L, "Filtro", "Filtro de óleo", new BigDecimal("35.00"));

        assertEquals(1L, peca.getId());
        assertEquals("Filtro", peca.getNome());
        assertEquals("Filtro de óleo", peca.getDescricao());
        assertEquals(new BigDecimal("35.00"), peca.getValor());
        assertTrue(peca.isAtivo());
    }

    @Test
    @DisplayName("deve atualizar dados válidos com sucesso")
    void shouldUpdateWithValidData() {
        Peca peca = new Peca(1L, "Filtro", "Filtro de óleo", new BigDecimal("35.00"), true);

        peca.atualizar("Pastilha", "Pastilha cerâmica", new BigDecimal("180.00"));

        assertEquals("Pastilha", peca.getNome());
        assertEquals("Pastilha cerâmica", peca.getDescricao());
        assertEquals(new BigDecimal("180.00"), peca.getValor());
    }

    @Test
    @DisplayName("deve falhar ao atualizar com nome inválido")
    void shouldFailWhenUpdatingWithInvalidNome() {
        Peca peca = new Peca(1L, "Filtro", "Filtro de óleo", new BigDecimal("35.00"), true);

        var valor = new BigDecimal("10.00");
        assertThrows(IllegalArgumentException.class,
                () -> peca.atualizar(" ", "Descrição", valor));
    }

    @Test
    @DisplayName("deve falhar ao atualizar com descrição inválida")
    void shouldFailWhenUpdatingWithInvalidDescricao() {
        Peca peca = new Peca(1L, "Filtro", "Filtro de óleo", new BigDecimal("35.00"), true);

        var valor = new BigDecimal("10.00");
        assertThrows(IllegalArgumentException.class,
                () -> peca.atualizar("Nome", " ", valor));
    }

    @Test
    @DisplayName("deve falhar ao atualizar com valor inválido")
    void shouldFailWhenUpdatingWithInvalidValor() {
        Peca peca = new Peca(1L, "Filtro", "Filtro de óleo", new BigDecimal("35.00"), true);

        assertThrows(IllegalArgumentException.class,
                () -> peca.atualizar("Nome", "Descrição", BigDecimal.ZERO));
    }

    @Test
    @DisplayName("deve falhar no construtor com nome inválido")
    void shouldFailOnConstructorWithInvalidNome() {
        var valor = new BigDecimal("10.00");
        assertThrows(IllegalArgumentException.class,
                () -> new Peca(1L, "", "Descrição", valor, true));
    }

    @Test
    @DisplayName("deve falhar no construtor com descrição inválida")
    void shouldFailOnConstructorWithInvalidDescricao() {
        var valor = new BigDecimal("10.00");
        assertThrows(IllegalArgumentException.class,
                () -> new Peca(1L, "Nome", "", valor, true));
    }

    @Test
    @DisplayName("deve falhar no construtor com valor inválido")
    void shouldFailOnConstructorWithInvalidValor() {
        var valor = new BigDecimal("0.00");
        assertThrows(IllegalArgumentException.class,
                () -> new Peca(1L, "Nome", "Descrição", valor, true));
    }

    @Test
    @DisplayName("deve desativar peça ativa com sucesso")
    void shouldDeactivateActivePeca() {
        Peca peca = new Peca(1L, "Filtro", "Filtro de óleo", new BigDecimal("35.00"), true);

        peca.desativar();

        assertFalse(peca.isAtivo());
    }

    @Test
    @DisplayName("deve falhar ao desativar peça já desativada")
    void shouldFailWhenDeactivatingAlreadyInactivePeca() {
        Peca peca = new Peca(1L, "Filtro", "Filtro de óleo", new BigDecimal("35.00"), false);

        assertThrows(IllegalArgumentException.class, peca::desativar);
    }

    @Test
    @DisplayName("deve implementar equals e hashCode por id")
    void shouldImplementEqualsAndHashCodeById() {
        Peca peca1 = new Peca(1L, "Filtro", "Filtro de óleo", new BigDecimal("35.00"), true);
        Peca peca2 = new Peca(1L, "Pastilha", "Pastilha dianteira", new BigDecimal("120.00"), true);
        Peca peca3 = new Peca(2L, "Óleo", "Óleo sintético", new BigDecimal("59.90"), true);

        assertEquals(peca1, peca2);
        assertEquals(peca1.hashCode(), peca2.hashCode());
        assertNotEquals(peca1, peca3);
    }

    @Test
    @DisplayName("deve gerar toString sem exceção")
    void shouldGenerateToStringWithoutException() {
        Peca peca = new Peca(1L, "Filtro", "Filtro de óleo", new BigDecimal("35.00"), true);

        assertDoesNotThrow(peca::toString);
    }
}
