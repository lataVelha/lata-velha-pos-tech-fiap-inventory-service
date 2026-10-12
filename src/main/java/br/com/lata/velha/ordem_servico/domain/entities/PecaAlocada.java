package br.com.lata.velha.ordem_servico.domain.entities;

import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

public final class PecaAlocada {
    private final Long id;
    private final Long pecaId;
    private final Long execucaoServicoId;

    private BigDecimal valorUnitarioPeca;

    private Integer quantidadeSolicitada;
    private Integer quantidadeReservada;
    private Integer quantidadeEncomendada;
    private Integer quantidadeInstalada;

    private StatusPecaAlocada status;
    private LocalDateTime atualizado;

    public PecaAlocada(Long id, Long pecaId, Long execucaoServicoId, BigDecimal valorUnitarioPeca, Integer quantidadeSolicitada, Integer quantidadeReservada, Integer quantidadeEncomendada, Integer quantidadeInstalada, StatusPecaAlocada status, LocalDateTime atualizado) {
        validatePecaId(pecaId);
        validateExecucaoServicoId(execucaoServicoId);

        this.id = id;
        this.pecaId = pecaId;
        this.execucaoServicoId = execucaoServicoId;
        setValorUnitarioPeca(valorUnitarioPeca);

        setQuantidadeSolicitada(quantidadeSolicitada);
        this.quantidadeReservada = quantidadeReservada;
        this.quantidadeEncomendada = quantidadeEncomendada;
        this.quantidadeInstalada = quantidadeInstalada;
        this.status = status;
        this.atualizado = atualizado;
    }

    public static PecaAlocada create(Long pecaId, Long execucaoServicoId, BigDecimal valorPecaUnitaria, Integer quantidadeSolicitada) {
        var quantidadeReservadaInicial = 0;
        var quantidadeEncomendadaInicial = 0;
        var quantidadeInstaladaInicial = 0;
        var statusInicial = StatusPecaAlocada.PENDENTE;
        return new PecaAlocada(null, pecaId, execucaoServicoId, valorPecaUnitaria, quantidadeSolicitada, quantidadeReservadaInicial, quantidadeEncomendadaInicial, quantidadeInstaladaInicial, statusInicial, LocalDateTime.now());
    }

    public void reservar(PecaEstoque estoque) {
        if (estoque != null && estoque.getQuantidadeDisponivel() > 0)
            reservarEstoque(estoque);
        encomendarFaltante();
        if(this.quantidadeEncomendada.equals(this.quantidadeSolicitada)) {
            this.status = StatusPecaAlocada.ENCOMENDA;
        } else if (this.quantidadeEncomendada != 0) {
            this.status = StatusPecaAlocada.PARCIAL;
        } else {
            this.status = StatusPecaAlocada.RESERVADA;
        }
        touch();
    }

    public void instalar(Integer quantidade) {
        if (quantidade == null || quantidade <= 0)
            throw new IllegalArgumentException("Quantidade inválida");

        var reservadasDisponiveis = this.quantidadeReservada - this.quantidadeInstalada;
        if (reservadasDisponiveis < quantidade)
            throw new IllegalStateException("Quantidade reservada insuficiente para instalar");

        this.quantidadeInstalada += quantidade;

        if (this.quantidadeInstalada.equals(this.quantidadeSolicitada))
            this.status = StatusPecaAlocada.INSTALADA;

        touch();
    }

    /* ================= HELPERS ================= */
    public boolean isAguardandoPeca() {
        return this.status.equals(StatusPecaAlocada.ENCOMENDA) ||
                this.status.equals(StatusPecaAlocada.PARCIAL);
    }

    public boolean isReservada() {
        return this.status == StatusPecaAlocada.RESERVADA;
    }

    public boolean isInstalada() {
        return this.status == StatusPecaAlocada.INSTALADA;
    }

    private void touch() {
        this.atualizado = LocalDateTime.now();
    }
    /* ================= GETTERS ================= */

    public Long getId() { return id; }
    public Long getPecaId() { return pecaId; }
    public Long getExecucaoServicoId() { return execucaoServicoId; }
    public BigDecimal getValorUnitarioPeca() { return valorUnitarioPeca; }
    public Integer getQuantidadeSolicitada() { return quantidadeSolicitada; }
    public Integer getQuantidadeReservada() { return quantidadeReservada; }
    public Integer getQuantidadeEncomendada() { return quantidadeEncomendada; }
    public Integer getQuantidadeInstalada() { return quantidadeInstalada; }
    public StatusPecaAlocada getStatus() { return status; }
    public LocalDateTime getAtualizado() { return atualizado; }

    public void setStatus(StatusPecaAlocada status) {
        this.status = status;
    }

    /* ================= OBJECT ================= */

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PecaAlocada)) return false;
        PecaAlocada that = (PecaAlocada) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        //TODO revert this to only ID when using proper UUID
        if(id == null)
            return Objects.hash(getPecaId(), getExecucaoServicoId());
        return Objects.hash(getId());
    }

    private void validateExecucaoServicoId(Long execucaoServicoId) {
        if (execucaoServicoId == null)
            throw new IllegalArgumentException("Serviço obrigatório");
    }

    private void validatePecaId(Long pecaId) {
        if (pecaId == null)
            throw new IllegalArgumentException("Peça obrigatória");
    }

    private void reservarEstoque(PecaEstoque estoque) {
        int quantidadeReservar = Math.min(estoque.getQuantidadeDisponivel(), getRestante());
        estoque.alocar(quantidadeReservar);
        this.quantidadeReservada += quantidadeReservar;
    }

    private void encomendarFaltante() {
        this.quantidadeEncomendada = getRestante();
    }

    private int getRestante() {
        return this.quantidadeSolicitada - this.quantidadeReservada;
    }

    private void setQuantidadeSolicitada(Integer quantidadeSolicitada) {
        if (quantidadeSolicitada == null || quantidadeSolicitada <= 0)
            throw new IllegalArgumentException("Quantidade inválida");
        this.quantidadeSolicitada = quantidadeSolicitada;
    }

    private void setValorUnitarioPeca(BigDecimal valorUnitarioPeca) {
        if (valorUnitarioPeca.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("valor unitário da peca deve ser positivo!");
        this.valorUnitarioPeca = valorUnitarioPeca;
    }

    public BigDecimal getValorTotal() {
        var quantidade = BigDecimal.valueOf(this.quantidadeSolicitada);
        return this.valorUnitarioPeca.multiply(quantidade);
    }
}