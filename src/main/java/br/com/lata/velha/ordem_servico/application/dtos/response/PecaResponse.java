package br.com.lata.velha.ordem_servico.application.dtos.response;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;

import java.math.BigDecimal;

public record PecaResponse(
    Long id,
    String nome,
    String descricao,
    BigDecimal valor,
    boolean ativo
) {
    public static PecaResponse from(Peca peca) {
        return new PecaResponse(
                peca.getId(),
                peca.getNome(),
                peca.getDescricao(),
                peca.getValor(),
                peca.isAtivo()
        );
    }
}
