package br.com.lata.velha.ordem_servico.infrastructure.persistence.migrations;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class V001CriarEstruturaEIndicesTest {

    @Mock
    private MongoTemplate mongoTemplate;

    @Mock
    private IndexOperations indexOperations;

    @Test
    @DisplayName("deve criar coleções e índices quando ainda não existem")
    void deveCriarEstruturaQuandoAusente() {
        when(mongoTemplate.collectionExists(anyString())).thenReturn(false);
        when(mongoTemplate.indexOps(anyString())).thenReturn(indexOperations);

        new V001CriarEstruturaEIndices().executar(mongoTemplate);

        verify(mongoTemplate).createCollection("PECA");
        verify(mongoTemplate).createCollection("PECA_ESTOQUE");
        verify(mongoTemplate).createCollection("PECA_ALOCADA");
        verify(mongoTemplate, never()).indexOps("PECA_ESTOQUE");
        verify(mongoTemplate).indexOps("PECA_ALOCADA");
        verify(indexOperations, times(1)).ensureIndex(any(IndexDefinition.class));
    }

    @Test
    @DisplayName("não deve recriar coleções já existentes")
    void naoDeveRecriarColecoesExistentes() {
        when(mongoTemplate.collectionExists(anyString())).thenReturn(true);
        when(mongoTemplate.indexOps(anyString())).thenReturn(indexOperations);

        new V001CriarEstruturaEIndices().executar(mongoTemplate);

        verify(mongoTemplate, never()).createCollection(anyString());
        verify(mongoTemplate, never()).indexOps("PECA_ESTOQUE");
        verify(indexOperations, times(1)).ensureIndex(any(IndexDefinition.class));
    }

    @Test
    @DisplayName("rollback deve remover as coleções na ordem inversa")
    void deveReverterEstrutura() {
        new V001CriarEstruturaEIndices().reverter(mongoTemplate);

        verify(mongoTemplate).dropCollection("PECA_ALOCADA");
        verify(mongoTemplate).dropCollection("PECA_ESTOQUE");
        verify(mongoTemplate).dropCollection("PECA");
    }
}
