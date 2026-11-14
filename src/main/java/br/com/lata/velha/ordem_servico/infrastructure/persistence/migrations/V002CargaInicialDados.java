package br.com.lata.velha.ordem_servico.infrastructure.persistence.migrations;

import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEntity;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEstoqueEntity;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories.PecaEstoqueMongoRepository;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories.PecaMongoRepository;
import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.math.BigDecimal;
import java.util.List;

@ChangeUnit(id = "v002-carga-inicial-pecas-estoque", order = "002", author = "lata-velha")
public class V002CargaInicialDados {

    @Execution
    public void executar(PecaMongoRepository pecaRepository,
                         PecaEstoqueMongoRepository estoqueRepository,
                         MongoTemplate mongoTemplate) {
        // O V001 antigo criava um índice unique em PECA_ID, mas o ID do
        // estoque é o @Id (mapeado para _id). Como o campo PECA_ID não existe
        // no documento, o índice barrava o segundo insert do seed com
        // "E11000 duplicate key ... PECA_ID: null". Remove o índice legado
        // antes de semear; em bases novas/corrigidas ele não existe.
        try {
            mongoTemplate.indexOps("PECA_ESTOQUE").dropIndex("PECA_ID_1");
        } catch (RuntimeException ignored) {
            // índice inexistente — nada a fazer
        }

        // 1. Salva as peças e captura os objetos já com os IDs gerados pelo Mongo
        PecaEntity p1 = pecaRepository.save(new PecaEntity(null, "Filtro de óleo", "Filtro para motor", new BigDecimal("35.00"), true));
        PecaEntity p2 = pecaRepository.save(new PecaEntity(null, "Óleo 5W30", "Óleo sintético", new BigDecimal("59.90"), true));
        PecaEntity p3 = pecaRepository.save(new PecaEntity(null, "Parafuso roda", "Parafuso fixação roda", new BigDecimal("5.00"), true));
        PecaEntity p4 = pecaRepository.save(new PecaEntity(null, "Disco freio", "Disco freio roda dianteira", new BigDecimal("200.00"), true));
        PecaEntity p5 = pecaRepository.save(new PecaEntity(null, "Pastilha freio", "Pastilha de freio roda dianteira", new BigDecimal("100.00"), true));
        PecaEntity p6 = pecaRepository.save(new PecaEntity(null, "Pivor", "Pivor roda dianteira", new BigDecimal("70.00"), true));
        PecaEntity p7 = pecaRepository.save(new PecaEntity(null, "Amortecedor", "Amortecedor roda dianteira", new BigDecimal("270.00"), true));

        // 2. Insere o estoque vinculando aos IDs gerados (mantendo a Pastilha sem estoque, igual ao SQL original)
        List<PecaEstoqueEntity> estoques = List.of(
                criarEstoque(p1.getId(), 50, 50),
                criarEstoque(p2.getId(), 10, 10),
                criarEstoque(p3.getId(), 50, 50),
                criarEstoque(p4.getId(), 5, 5),
                criarEstoque(p6.getId(), 10, 10),
                criarEstoque(p7.getId(), 8, 8)
        );

        estoqueRepository.saveAll(estoques);
    }

    private PecaEstoqueEntity criarEstoque(String pecaId, int armazenada, int disponivel) {
        PecaEstoqueEntity estoque = new PecaEstoqueEntity();
        estoque.setPecaId(pecaId);
        estoque.setQuantidadeArmazenada(armazenada);
        estoque.setQuantidadeDisponivel(disponivel);
        return estoque;
    }

    @RollbackExecution
    public void reverter(PecaMongoRepository pecaRepository, PecaEstoqueMongoRepository estoqueRepository) {
        estoqueRepository.deleteAll();
        pecaRepository.deleteAll();
    }
}

