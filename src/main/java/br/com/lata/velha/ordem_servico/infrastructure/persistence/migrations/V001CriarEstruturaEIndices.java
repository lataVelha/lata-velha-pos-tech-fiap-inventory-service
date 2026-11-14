package br.com.lata.velha.ordem_servico.infrastructure.persistence.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
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

        // PECA_ESTOQUE usa o próprio ID da peça como @Id (mapeado para _id,
        // que já é único). O índice antigo em PECA_ID era incorreto: o campo
        // não existe no documento, então o unique barrava o segundo insert
        // da carga inicial com "dup key: { PECA_ID: null }".

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
