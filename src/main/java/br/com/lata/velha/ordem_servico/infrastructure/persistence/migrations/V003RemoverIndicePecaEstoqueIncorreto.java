package br.com.lata.velha.ordem_servico.infrastructure.persistence.migrations;

import io.mongock.api.annotations.ChangeUnit;
import io.mongock.api.annotations.Execution;
import io.mongock.api.annotations.RollbackExecution;
import org.springframework.data.mongodb.core.MongoTemplate;

/**
 * Remove o índice unique incorreto de PECA_ESTOQUE em bases onde o V001 antigo
 * já rodou. O ID do estoque é o @Id da entidade (mapeado para _id, único por
 * definição); o campo PECA_ID não existe no documento. Manter esse índice faz
 * qualquer segundo insert falhar com "dup key: { PECA_ID: null }".
 *
 * Em bases novas o V001 já não cria o índice e não há nada a remover.
 */
@ChangeUnit(id = "v003-remover-indice-peca-estoque-incorreto", order = "003", author = "lata-velha")
public class V003RemoverIndicePecaEstoqueIncorreto {

    @Execution
    public void executar(MongoTemplate mongoTemplate) {
        try {
            mongoTemplate.indexOps("PECA_ESTOQUE").dropIndex("PECA_ID_1");
        } catch (RuntimeException ignored) {
            // índice inexistente — base nova ou já corrigida
        }
    }

    @RollbackExecution
    public void reverter() {
        // Nada a reverter: o índice removido era incorreto.
    }
}
