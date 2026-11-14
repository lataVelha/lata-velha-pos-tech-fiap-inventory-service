package br.com.lata.velha.ordem_servico.infrastructure.persistence.migrations;

import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEntity;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEstoqueEntity;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories.PecaEstoqueMongoRepository;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories.PecaMongoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class V002CargaInicialDadosTest {

    @Mock
    private PecaMongoRepository pecaRepository;

    @Mock
    private PecaEstoqueMongoRepository estoqueRepository;

    @Test
    @DisplayName("deve inserir as peças e os estoques vinculados aos ids gerados")
    void deveCarregarDadosIniciais() {
        when(pecaRepository.save(any(PecaEntity.class))).thenAnswer(invocation -> {
            PecaEntity entity = invocation.getArgument(0);
            entity.setId("id-" + entity.getNome().replace(" ", "-"));
            return entity;
        });

        new V002CargaInicialDados().executar(pecaRepository, estoqueRepository);

        verify(pecaRepository, times(7)).save(any(PecaEntity.class));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<PecaEstoqueEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(estoqueRepository).saveAll(captor.capture());
        assertThat(captor.getValue()).hasSize(6);
        assertThat(captor.getValue())
                .allSatisfy(estoque -> {
                    assertThat(estoque.getPecaId()).isNotNull().startsWith("id-");
                    assertThat(estoque.getQuantidadeArmazenada()).isEqualTo(estoque.getQuantidadeDisponivel());
                });
        assertThat(captor.getValue())
                .noneMatch(estoque -> estoque.getPecaId().contains("Pastilha"));
    }

    @Test
    @DisplayName("rollback deve limpar estoques e peças")
    void deveReverterCargaInicial() {
        new V002CargaInicialDados().reverter(pecaRepository, estoqueRepository);

        verify(estoqueRepository).deleteAll();
        verify(pecaRepository).deleteAll();
    }
}
