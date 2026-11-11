package br.com.lata.velha.ordem_servico.infrastructure.persistence.entities;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Document(collection = "PECA_ALOCADA")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PecaAlocadaEntity {

    @Id
    private String id;

    @Field(name = "PECA_ID")
    private String pecaId;

    @Field(name = "EXECUCAO_SERVICO_ID")
    private Long execucaoServicoId;

    @Field(name = "VALOR_UNITARIO")
    private BigDecimal valorUnitario;

    @Field(name = "QTD_SOLICITADA")
    private Integer quantidadeSolicitada;

    @Field(name = "QTD_RESERVADA")
    private Integer quantidadeReservada = 0;

    @Field(name = "QTD_ENCOMENDADA")
    private Integer quantidadeEncomendada = 0;

    @Field(name = "QTD_INSTALADA")
    private Integer quantidadeInstalada = 0;

    @Field(name = "STATUS")
    private StatusPecaAlocada status;

    @Field(name = "ATUALIZADO_EM")
    private LocalDateTime atualizado;

    public static PecaAlocadaEntity fromDomain(PecaAlocada domain) {
        if (domain == null) return null;
        return new PecaAlocadaEntity(
                domain.getId(),
                domain.getPecaId(),
                domain.getExecucaoServicoId(),
                domain.getValorUnitarioPeca(),
                domain.getQuantidadeSolicitada(),
                domain.getQuantidadeReservada(),
                domain.getQuantidadeEncomendada(),
                domain.getQuantidadeInstalada(),
                domain.getStatus(),
                domain.getAtualizado()
        );
    }

    public PecaAlocada toDomain() {
        return new PecaAlocada(
                id,
                pecaId,
                execucaoServicoId,
                valorUnitario,
                quantidadeSolicitada,
                quantidadeReservada,
                quantidadeEncomendada,
                quantidadeInstalada,
                status,
                atualizado
        );
    }

    public void setPeca(PecaEntity peca) {
        this.pecaId = peca != null ? peca.getId() : null;
    }

    public PecaEntity getPeca() {
        if (this.pecaId == null) {
            return null;
        }

        PecaEntity peca = new PecaEntity();
        peca.setId(this.pecaId);
        return peca;
    }
}