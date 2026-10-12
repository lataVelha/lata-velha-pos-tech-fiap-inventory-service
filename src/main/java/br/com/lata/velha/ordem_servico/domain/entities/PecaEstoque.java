package br.com.lata.velha.ordem_servico.domain.entities;

public final class PecaEstoque {

    private final Long pecaId;
    private Integer quantidadeArmazenada;
    private Integer quantidadeDisponivel;

    public PecaEstoque(Long pecaId, Integer quantidadeArmazenada, Integer quantidadeDisponivel) {
        validatePecaId(pecaId);
        this.pecaId = pecaId;
        setQuantidadeArmazenada(quantidadeArmazenada);
        setQuantidadeDisponivel(quantidadeDisponivel);
    }

    public static PecaEstoque create(Long pecaId) {
        return new PecaEstoque(pecaId, 0, 0);
    }

    public Long getPecaId() {
        return pecaId;
    }

    public Integer getQuantidadeArmazenada() {
        return quantidadeArmazenada;
    }

    public Integer getQuantidadeDisponivel() {
        return quantidadeDisponivel;
    }

    public void adicionar(Integer quantidade) {
        if (quantidade == null || quantidade <= 0)
            throw new IllegalArgumentException("Quantidade de entrada inválida");
        this.quantidadeArmazenada += quantidade;
        this.quantidadeDisponivel += quantidade;
    }

    public void alocar(int quantidadeAlocar) {
        if(getQuantidadeDisponivel() < quantidadeAlocar)
            throw new IllegalArgumentException("Estoque: quantidade de peças para alocação indisponível");
        setQuantidadeDisponivel(this.quantidadeDisponivel - quantidadeAlocar);
    }

    public void retirar(Integer quantidade) {
        if (quantidade == null || quantidade <= 0)
            throw new IllegalArgumentException("Quantidade de saída inválida");

        if (this.quantidadeArmazenada < quantidade)
            throw new IllegalArgumentException("Estoque insuficiente para a peça informada");

        this.quantidadeArmazenada -= quantidade;
    }

    public void ajustar(Integer quantidadeArmazenada, Integer quantidadeDisponivel) {
        setQuantidadeArmazenada(quantidadeArmazenada);
        setQuantidadeDisponivel(quantidadeDisponivel);
    }

    @Override
    public String toString() {
        return "PecaEstoque{pecaId=" + pecaId +
                ", quantidade=" + quantidadeArmazenada + '}';
    }

    private void validatePecaId(Long pecaId) {
        if (pecaId == null || pecaId <= 0)
            throw new IllegalArgumentException("ID da peça inválido");
    }

    private void setQuantidadeArmazenada(Integer quantidadeArmazenada) {
        if (quantidadeArmazenada == null || quantidadeArmazenada < 0)
            throw new IllegalArgumentException("Quantidade armazenada inválida");
        this.quantidadeArmazenada = quantidadeArmazenada;
    }

    private void setQuantidadeDisponivel(Integer quantidadeDisponivel) {
        if(quantidadeDisponivel == null || quantidadeDisponivel < 0)
            throw new IllegalArgumentException("Quantidade disponível inválida");
        this.quantidadeDisponivel = quantidadeDisponivel;
    }
}