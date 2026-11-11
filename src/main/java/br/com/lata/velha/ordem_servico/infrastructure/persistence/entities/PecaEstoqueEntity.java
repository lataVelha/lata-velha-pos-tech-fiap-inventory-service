package br.com.lata.velha.ordem_servico.infrastructure.persistence.entities;

import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "PECA_ESTOQUE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PecaEstoqueEntity {

    @Id
    private String pecaId;

    @Field(name = "QUANTIDADE_ARMAZENADA")
    private Integer quantidadeArmazenada;

    @Field(name = "QUANTIDADE_DISPONIVEL")
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