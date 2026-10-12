package br.com.lata.velha.ordem_servico.infrastructure.persistence.entities;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "PECA")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PecaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID", nullable = false)
    private Long id;

    @Column(name = "NOME", nullable = false)
    private String nome;

    @Column(name = "DESCRICAO", nullable = false)
    private String descricao;

    @Column(name = "VALOR", nullable = false)
    private BigDecimal valor;

    @Column(name = "ATIVO", nullable = false)
    private boolean ativo = true;

    public static PecaEntity fromDomain(Peca domain) {
        return new PecaEntity(
                domain.getId(),
                domain.getNome(),
                domain.getDescricao(),
                domain.getValor(),
                domain.isAtivo()
        );
    }

    public Peca toDomain() {
        return new Peca(
                id,
                nome,
                descricao,
                valor,
                ativo
        );
    }
}