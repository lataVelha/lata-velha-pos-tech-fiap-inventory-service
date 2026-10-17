package br.com.lata.velha.ordem_servico.api.controllers;

import br.com.lata.velha.ordem_servico.application.controllers.peca.PecaCleanController;
import br.com.lata.velha.ordem_servico.application.dtos.request.AtualizarPecaRequest;
import br.com.lata.velha.ordem_servico.application.dtos.request.CadastrarPecaRequest;
import br.com.lata.velha.ordem_servico.application.dtos.response.PecaResponse;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/pecas")
@Tag(name = "Peças", description = "Catálogo de peças utilizadas nos serviços da oficina. Cada peça possui um valor unitário e um controle de estoque associado. As peças devem ser cadastradas aqui antes de serem adicionadas aos serviços de uma OS.")
public class PecaController {

    private final PecaCleanController cleanController;

    @PostMapping
    @Transactional
    @Operation(summary = "Cadastrar nova peça", description = "Registra uma nova peça no catálogo. Após o cadastro, o estoque é iniciado automaticamente com saldo zero — use `POST /pecas/{pecaId}/estoque/entrada` para adicionar estoque.")
    @ApiResponse(responseCode = "201", description = "Peça criada")
    public ResponseEntity<PecaResponse> cadastrar(@Valid @RequestBody CadastrarPecaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cleanController.cadastrar(request));
    }

    @GetMapping
    @Operation(summary = "Listar todas as peças ativas", description = "Retorna lista de peças ativas no sistema")
    @ApiResponse(responseCode = "200", description = "Peças listadas")
    public ResponseEntity<PaginatedResult<PecaResponse>> listar(
            @Parameter(description = "Número da página (começa em 0)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Itens por página") @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(cleanController.buscarTodos(page, size));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Buscar peça por ID")
    @ApiResponse(responseCode = "200", description = "Peça encontrada")
    @ApiResponse(responseCode = "404", description = "Peça não encontrada")
    public ResponseEntity<PecaResponse> buscarPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cleanController.buscarPorId(id));
    }

    @PutMapping("/{id}")
    @Transactional
    @Operation(summary = "Atualizar informações da peça")
    @ApiResponse(responseCode = "200", description = "Peça atualizada")
    @ApiResponse(responseCode = "404", description = "Peça não encontrada")
    public ResponseEntity<PecaResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarPecaRequest request) {
        return ResponseEntity.ok(cleanController.atualizar(id, request));
    }

    @PatchMapping("/{id}/desativar")
    @Transactional
    @Operation(summary = "Desativar peça (Soft Delete)", description = "Inativa a peça do catálogo. Peças desativadas não podem ser adicionadas a novos serviços, mas OS existentes que já utilizam a peça não são afetadas.")
    @ApiResponse(responseCode = "204", description = "Peça desativada")
    @ApiResponse(responseCode = "404", description = "Peça não encontrada")
    public ResponseEntity<Void> desativar(@PathVariable Long id) {
        cleanController.desativar(id);
        return ResponseEntity.noContent().build();
    }
}
