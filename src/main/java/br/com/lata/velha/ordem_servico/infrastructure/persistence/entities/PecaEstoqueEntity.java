package br.com.lata.velha.ordem_servico.infrastructure.persistence.entities;

import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "PECA_ESTOQUE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PecaEstoqueEntity {

    @Id
    @Column(name = "PECA_ID", nullable = false)
    private Long pecaId;

    @Column(name = "QUANTIDADE_ARMAZENADA", nullable = false)
    private Integer quantidadeArmazenada;

    @Column(name = "QUANTIDADE_DISPONIVEL", nullable = false)
    private Integer quantidadeDisponivel;

    public static PecaEstoqueEntity fromDomain(PecaEstoque domain) {
        return new PecaEstoqueEntity(
                domain.getPecaId(),
                domain.getQuantidadeArmazenada(),
                domain.getQuantidadeDisponivel()
        );
    }

    public PecaEstoque toDomain() {
        return new PecaEstoque(this.pecaId, this.quantidadeArmazenada, this.quantidadeDisponivel);
    }
}