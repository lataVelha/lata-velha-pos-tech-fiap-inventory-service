package br.com.lata.velha.ordem_servico.infrastructure.persistence.migrations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexOperations;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class V003RemoverIndicePecaEstoqueIncorretoTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private IndexOperations indexOperations;

    @Test
    @DisplayName("deve remover o índice legacy PECA_ID_1")
    void deveRemoverIndiceLegacy() {
        when(mongoTemplate.indexOps("PECA_ESTOQUE")).thenReturn(indexOperations);

        new V003RemoverIndicePecaEstoqueIncorreto().executar(mongoTemplate);

        verify(indexOperations).dropIndex("PECA_ID_1");
    }

    @Test
    @DisplayName("deve ignorar quando o índice já não existe")
    void deveIgnorarIndiceInexistente() {
        when(mongoTemplate.indexOps("PECA_ESTOQUE"))
                .thenThrow(new IllegalArgumentException("collection not found"));

        assertThatCode(() -> new V003RemoverIndicePecaEstoqueIncorreto().executar(mongoTemplate))
                .doesNotThrowAnyException();
    }
}
