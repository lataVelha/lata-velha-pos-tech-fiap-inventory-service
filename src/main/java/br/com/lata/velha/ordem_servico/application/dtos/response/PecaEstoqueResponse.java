package br.com.lata.velha.ordem_servico.application.dtos.response;

import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;

public record PecaEstoqueResponse(
        Long pecaId,
        Integer quantidadeArmazenada,
        Integer quantidadeDisponivel
) {
    public static PecaEstoqueResponse from(PecaEstoque estoque) {
        return new PecaEstoqueResponse(estoque.getPecaId(), estoque.getQuantidadeArmazenada(), estoque.getQuantidadeDisponivel());
    }
}
