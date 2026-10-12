package br.com.lata.velha.ordem_servico.domain.entities;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PecaEstoqueTest {

    @Test
    void deveCriarEstoqueQuandoDadosValidos() {
        PecaEstoque estoque = new PecaEstoque(1L, 5, 5);

        assertThat(estoque.getPecaId()).isEqualTo(1L);
        assertThat(estoque.getQuantidadeArmazenada()).isEqualTo(5);
        assertThat(estoque.getQuantidadeDisponivel()).isEqualTo(5);
    }

    @Test
    void deveCriarEstoqueViaFactoryMethod() {
        PecaEstoque estoque = PecaEstoque.create(1L);

        assertThat(estoque.getPecaId()).isEqualTo(1L);
        assertThat(estoque.getQuantidadeArmazenada()).isZero();
        assertThat(estoque.getQuantidadeDisponivel()).isZero();
    }

    @Test
    void deveFalharQuandoPecaIdForNulo() {
        assertThatThrownBy(() -> new PecaEstoque(null, 5, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID da peça inválido");
    }

    @Test
    void deveFalharQuandoPecaIdForMenorOuIgualAZero() {
        assertThatThrownBy(() -> new PecaEstoque(0L, 5, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID da peça inválido");
    }

    @Test
    void deveFalharQuandoQuantidadeArmazenadaForNula() {
        assertThatThrownBy(() -> new PecaEstoque(1L, null, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade armazenada inválida");
    }

    @Test
    void deveFalharQuandoQuantidadeArmazenadaForNegativa() {
        assertThatThrownBy(() -> new PecaEstoque(1L, -1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade armazenada inválida");
    }

    @Test
    void deveFalharQuandoQuantidadeDisponivelForNula() {
        assertThatThrownBy(() -> new PecaEstoque(1L, 5, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade disponível inválida");
    }

    @Test
    void deveFalharQuandoQuantidadeDisponivelForNegativa() {
        assertThatThrownBy(() -> new PecaEstoque(1L, 5, -1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade disponível inválida");
    }

    @Test
    void deveAdicionarQuantidadeAoEstoque() {
        PecaEstoque estoque = new PecaEstoque(1L, 10, 10);

        estoque.adicionar(5);

        assertThat(estoque.getQuantidadeArmazenada()).isEqualTo(15);
        assertThat(estoque.getQuantidadeDisponivel()).isEqualTo(15);
    }

    @Test
    void deveFalharAoAdicionarQuantidadeNula() {
        PecaEstoque estoque = new PecaEstoque(1L, 10, 10);

        assertThatThrownBy(() -> estoque.adicionar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade de entrada inválida");
    }

    @Test
    void deveFalharAoAdicionarQuantidadeZeroOuNegativa() {
        PecaEstoque estoque = new PecaEstoque(1L, 10, 10);

        assertThatThrownBy(() -> estoque.adicionar(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade de entrada inválida");
    }

    @Test
    void deveAlocarQuantidadeDisponivel() {
        PecaEstoque estoque = new PecaEstoque(1L, 10, 10);

        estoque.alocar(4);

        assertThat(estoque.getQuantidadeDisponivel()).isEqualTo(6);
        assertThat(estoque.getQuantidadeArmazenada()).isEqualTo(10);
    }

    @Test
    void deveFalharAoAlocarMaisQueDisponivel() {
        PecaEstoque estoque = new PecaEstoque(1L, 10, 3);

        assertThatThrownBy(() -> estoque.alocar(5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Estoque: quantidade de peças para alocação indisponível");
    }

    @Test
    void deveRetirarQuantidadeDoEstoque() {
        PecaEstoque estoque = new PecaEstoque(1L, 10, 10);

        estoque.retirar(3);

        assertThat(estoque.getQuantidadeArmazenada()).isEqualTo(7);
    }

    @Test
    void deveFalharAoRetirarQuantidadeNula() {
        PecaEstoque estoque = new PecaEstoque(1L, 10, 5);

        assertThatThrownBy(() -> estoque.retirar(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade de saída inválida");
    }

    @Test
    void deveFalharAoRetirarQuantidadeZeroOuNegativa() {
        PecaEstoque estoque = new PecaEstoque(1L, 10, 5);

        assertThatThrownBy(() -> estoque.retirar(0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade de saída inválida");
    }

    @Test
    void deveFalharAoRetirarMaisQueArmazenado() {
        PecaEstoque estoque = new PecaEstoque(1L, 5, 5);

        assertThatThrownBy(() -> estoque.retirar(6))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Estoque insuficiente para a peça informada");
    }

    @Test
    void deveAjustarEstoqueComNovasQuantidades() {
        PecaEstoque estoque = new PecaEstoque(1L, 10, 10);

        estoque.ajustar(20, 15);

        assertThat(estoque.getQuantidadeArmazenada()).isEqualTo(20);
        assertThat(estoque.getQuantidadeDisponivel()).isEqualTo(15);
    }

    @Test
    void deveFalharAoAjustarComQuantidadesInvalidas() {
        PecaEstoque estoque = new PecaEstoque(1L, 10, 10);

        assertThatThrownBy(() -> estoque.ajustar(-1, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Quantidade armazenada inválida");
    }

    @Test
    void deveRetornarToString() {
        PecaEstoque estoque = new PecaEstoque(1L, 5, 5);

        assertThat(estoque.toString()).contains("pecaId=1").contains("quantidade=5");
    }
}
