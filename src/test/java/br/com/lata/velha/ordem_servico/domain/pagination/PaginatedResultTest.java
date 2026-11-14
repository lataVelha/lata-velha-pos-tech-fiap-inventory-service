package br.com.lata.velha.ordem_servico.domain.pagination;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PaginatedResultTest {

    @Test
    @DisplayName("map deve transformar o conteúdo e preservar os metadados de paginação")
    void deveMapearConteudoPreservandoMetadados() {
        var original = new PaginatedResult<>(List.of("a", "bb", "ccc"), 2, 5, 13, 3);

        var resultado = PaginatedResult.map(original, String::length);

        assertThat(resultado.content()).containsExactly(1, 2, 3);
        assertThat(resultado.page()).isEqualTo(2);
        assertThat(resultado.size()).isEqualTo(5);
        assertThat(resultado.totalElements()).isEqualTo(13);
        assertThat(resultado.totalPages()).isEqualTo(3);
    }

    @Test
    @DisplayName("map deve manter lista vazia quando não há conteúdo")
    void deveMapearListaVazia() {
        var original = new PaginatedResult<String>(List.of(), 0, 10, 0, 0);

        var resultado = PaginatedResult.map(original, String::length);

        assertThat(resultado.content()).isEmpty();
    }
}
