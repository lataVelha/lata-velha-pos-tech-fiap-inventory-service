package br.com.lata.velha.ordem_servico.application.dtos.response;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record PecaServicoResponse(

        @Schema(example = "10")
        Long alocacaoId,

        @Schema(example = "1")
        Long id,

        @Schema(example = "Pastilha de freio")
        String nome,

        @Schema(example = "49.90")
        BigDecimal valor,

        @Schema(example = "4")
        Integer quantidadeSolicitada,

        @Schema(example = "2")
        Integer quantidadeReservada,

        @Schema(example = "2")
        Integer quantidadeEncomendada,

        @Schema(example = "PARCIAL")
        String status,

        @Schema(example = "2026-04-11T12:00:00")
        LocalDateTime atualizado

) {
    public static PecaServicoResponse from(PecaAlocada domain, String nome, BigDecimal valor) {
        return new PecaServicoResponse(
                domain.getId(),
                domain.getPecaId(),
                nome,
                valor,
                domain.getQuantidadeSolicitada(),
                domain.getQuantidadeReservada(),
                domain.getQuantidadeEncomendada(),
                domain.getStatus() != null ? domain.getStatus().name() : null,
                domain.getAtualizado()
        );
    }
}