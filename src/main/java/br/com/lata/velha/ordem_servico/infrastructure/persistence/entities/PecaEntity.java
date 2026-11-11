package br.com.lata.velha.ordem_servico.infrastructure.persistence.entities;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;

@Document(collection = "PECA")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PecaEntity {

    @Id
    private String id;

    @Field(name = "NOME")
    private String nome;

    @Field(name = "DESCRICAO")
    private String descricao;

    @Field(name = "VALOR")
    private BigDecimal valor;

    @Field(name = "ATIVO")
    private boolean ativo = true;

    public static PecaEntity fromDomain(Peca domain) {
        return new PecaEntity(
                domain.getId() != null ? domain.getId(): null,
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
