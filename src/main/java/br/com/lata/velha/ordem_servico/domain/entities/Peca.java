package br.com.lata.velha.ordem_servico.domain.entities;

import java.math.BigDecimal;
import java.util.Objects;

public class Peca {

    private Long id;
    private String nome;
    private String descricao;
    private BigDecimal valor;
    private boolean ativo = true;

    public Peca(Long id, String nome, String descricao, BigDecimal valor) {
        this(id, nome, descricao, valor, true);
    }

    public Peca(Long id, String nome, String descricao, BigDecimal valor, boolean ativo) {
        this.id = id;
        atualizar(nome, descricao, valor);
        this.ativo = ativo;
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void atualizar(String nome, String descricao, BigDecimal valor) {
        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome da peça não pode ser vazio");
        }
        if (descricao == null || descricao.isBlank()) {
            throw new IllegalArgumentException("Descrição da peça não pode ser vazia");
        }
        if (valor == null || valor.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor da peça deve ser maior que zero");
        }

        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
    }

    public void desativar() {
        if (!this.ativo) {
            throw new IllegalArgumentException("Peça já está desativada");
        }
        this.ativo = false;
    }

    public boolean isAtivo() {
        return ativo;
    }

    @Override
    public String toString() {
        return "Peca{id=" + id + ", nome='" + nome + "', valor=" + valor + ", ativo=" + ativo + "}";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Peca)) return false;
        Peca peca = (Peca) o;
        return Objects.equals(id, peca.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}