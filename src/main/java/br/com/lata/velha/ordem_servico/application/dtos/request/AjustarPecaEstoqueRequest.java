package br.com.lata.velha.ordem_servico.application.dtos.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record AjustarPecaEstoqueRequest(
        @NotNull(message = "Quantidade armazenada é obrigatória")
        @PositiveOrZero(message = "Quantidade armazenada não pode ser negativa")
        Integer quantidadeArmazenada,

        @NotNull(message = "Quantidade disponível é obrigatória")
        @PositiveOrZero(message = "Quantidade disponível não pode ser negativa")
        Integer quantidadeDisponivel
) {
}
