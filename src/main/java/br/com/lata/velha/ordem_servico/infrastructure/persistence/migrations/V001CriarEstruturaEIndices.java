package br.com.lata.velha.ordem_servico.infrastructure.persistence.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.Index;
import org.bson.Document;

@ChangeUnit(id = "v001-criar-estrutura-indices", order = "001", author = "lata-velha")
public class V001CriarEstruturaEIndices {

    @Execution
    public void executar(MongoTemplate mongoTemplate) {
        if (!mongoTemplate.collectionExists("PECA")) {
            mongoTemplate.createCollection("PECA");
        }
        if (!mongoTemplate.collectionExists("PECA_ESTOQUE")) {
            mongoTemplate.createCollection("PECA_ESTOQUE");
        }
        if (!mongoTemplate.collectionExists("PECA_ALOCADA")) {
            mongoTemplate.createCollection("PECA_ALOCADA");
        }

        // Garante que cada peça tenha apenas um registro de estoque
        mongoTemplate.indexOps("PECA_ESTOQUE")
                .ensureIndex(new Index().on("PECA_ID", Sort.Direction.ASC).unique());

        // Equivale ao CONSTRAINT UK_PECA_ALOCADA UNIQUE (EXECUCAO_SERVICO_ID, PECA_ID)
        Document ukFields = new Document()
                .append("EXECUCAO_SERVICO_ID", 1)
                .append("PECA_ID", 1);

        mongoTemplate.indexOps("PECA_ALOCADA")
                .ensureIndex(new CompoundIndexDefinition(ukFields).named("UK_PECA_ALOCADA").unique());
    }

    @RollbackExecution
    public void reverter(MongoTemplate mongoTemplate) {
        mongoTemplate.dropCollection("PECA_ALOCADA");
        mongoTemplate.dropCollection("PECA_ESTOQUE");
        mongoTemplate.dropCollection("PECA");
    }
}
