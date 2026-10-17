package br.com.lata.velha.ordem_servico.api.controllers;

import br.com.lata.velha.ordem_servico.application.controllers.pecaestoque.PecaEstoqueCleanController;
import br.com.lata.velha.ordem_servico.application.dtos.request.AjustarPecaEstoqueRequest;
import br.com.lata.velha.ordem_servico.application.dtos.request.MovimentarPecaEstoqueRequest;
import br.com.lata.velha.ordem_servico.application.dtos.response.PecaEstoqueResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pecas/{pecaId}/estoque")
@Tag(
        name = "Estoque de Peças",
        description = "Controle de estoque. `quantidadeArmazenada` = total físico; `quantidadeDisponivel` = total menos reservas de OS ativas. Peças são reservadas ao aprovar serviços e baixadas ao retirar o veículo."
)
public class PecaEstoqueController {

    private final PecaEstoqueCleanController cleanController;

    @GetMapping
    @Operation(summary = "Consultar estoque da peça", description = "Retorna quantidade armazenada e disponível (descontando reservas de OS ativas).")
    @ApiResponse(responseCode = "200", description = "Estoque consultado com sucesso")
    @ApiResponse(responseCode = "404", description = "Peça não encontrada ou estoque ainda não inicializado")
    public ResponseEntity<PecaEstoqueResponse> buscar(@PathVariable Long pecaId) {
        return ResponseEntity.ok(cleanController.buscar(pecaId));
    }

    @PostMapping("/entrada")
    @Transactional
    @Operation(summary = "Registrar entrada no estoque", description = "Incrementa a quantidade armazenada. Use ao receber peças de fornecedores.")
    @ApiResponse(responseCode = "200", description = "Entrada registrada — retorna estoque atualizado")
    @ApiResponse(responseCode = "400", description = "Quantidade inválida (deve ser maior que zero)")
    @ApiResponse(responseCode = "404", description = "Peça não encontrada")
    public ResponseEntity<PecaEstoqueResponse> entrada(
            @PathVariable Long pecaId,
            @Valid @RequestBody MovimentarPecaEstoqueRequest request) {
        return ResponseEntity.ok(cleanController.entrada(pecaId, request));
    }

    @PostMapping("/saida")
    @Transactional
    @Operation(summary = "Registrar saída manual do estoque", description = "Saída manual de estoque. Saídas vinculadas a OS são processadas automaticamente ao retirar o veículo.")
    @ApiResponse(responseCode = "200", description = "Saída registrada — retorna estoque atualizado")
    @ApiResponse(responseCode = "400", description = "Quantidade inválida ou saldo insuficiente para a saída solicitada")
    @ApiResponse(responseCode = "404", description = "Peça ou estoque não encontrado")
    public ResponseEntity<PecaEstoqueResponse> saida(
            @PathVariable Long pecaId,
            @Valid @RequestBody MovimentarPecaEstoqueRequest request) {
        return ResponseEntity.ok(cleanController.saida(pecaId, request));
    }

    @PatchMapping("/ajuste")
    @Transactional
    @Operation(summary = "Ajustar saldo do estoque", description = "Substitui o saldo atual pelo valor informado. Use para corrigir divergências de inventário.")
    @ApiResponse(responseCode = "200", description = "Saldo ajustado para o valor informado")
    @ApiResponse(responseCode = "400", description = "Valor de ajuste inválido (não pode ser negativo)")
    @ApiResponse(responseCode = "404", description = "Peça não encontrada")
    public ResponseEntity<PecaEstoqueResponse> ajustar(
            @PathVariable Long pecaId,
            @Valid @RequestBody AjustarPecaEstoqueRequest request) {
        return ResponseEntity.ok(cleanController.ajustar(pecaId, request));
    }
}
