package br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories;

import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEstoqueEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PecaEstoqueRepositoryImplTest {

    @Mock
    private PecaEstoqueJpaRepository jpaRepository;

    @InjectMocks
    private PecaEstoqueRepositoryImpl repository;

    private PecaEstoque domainEstoque() {
        return new PecaEstoque(1L, 10, 8);
    }

    private PecaEstoqueEntity entityEstoque() {
        return new PecaEstoqueEntity(1L, 10, 8);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("deve salvar e retornar domínio mapeado")
        void deveSalvarERetornarDominio() {
            when(jpaRepository.save(any())).thenReturn(entityEstoque());

            PecaEstoque result = repository.save(domainEstoque());

            assertThat(result.getPecaId()).isEqualTo(1L);
            assertThat(result.getQuantidadeArmazenada()).isEqualTo(10);
            assertThat(result.getQuantidadeDisponivel()).isEqualTo(8);
            verify(jpaRepository).save(any());
        }
    }

    @Nested
    @DisplayName("saveAll")
    class SaveAll {

        @Test
        @DisplayName("deve salvar coleção e retornar domínios mapeados")
        void deveSalvarColecaoERetornarDominios() {
            when(jpaRepository.saveAll(any())).thenReturn(List.of(entityEstoque()));

            var result = repository.saveAll(List.of(domainEstoque()));

            assertThat(result).hasSize(1);
            assertThat(result.iterator().next().getPecaId()).isEqualTo(1L);
            verify(jpaRepository).saveAll(any());
        }

        @Test
        @DisplayName("deve retornar lista vazia quando coleção vazia")
        void deveRetornarVazioQuandoColecaoVazia() {
            when(jpaRepository.saveAll(any())).thenReturn(List.of());

            var result = repository.saveAll(List.of());

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPecaId")
    class FindByPecaId {

        @Test
        @DisplayName("deve retornar Optional com estoque quando encontrado")
        void deveRetornarQuandoEncontrado() {
            when(jpaRepository.findById(1L)).thenReturn(Optional.of(entityEstoque()));

            var result = repository.findByPecaId(1L);

            assertThat(result).isPresent();
            assertThat(result.get().getPecaId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("deve retornar Optional vazio quando não encontrado")
        void deveRetornarVazioQuandoNaoEncontrado() {
            when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

            var result = repository.findByPecaId(99L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("baixarEstoque")
    class BaixarEstoque {

        @Test
        @DisplayName("deve delegar ao JPA repository")
        void deveDelegarAoJpaRepository() {
            repository.baixarEstoque(1L, 3);

            verify(jpaRepository).baixarEstoque(1L, 3);
        }
    }

    @Nested
    @DisplayName("findAllByPecaIds")
    class FindAllByPecaIds {

        @Test
        @DisplayName("deve retornar lista de estoques mapeados")
        void deveRetornarListaMapeada() {
            when(jpaRepository.findAllByPecaIdIn(Set.of(1L, 2L)))
                    .thenReturn(List.of(entityEstoque(), new PecaEstoqueEntity(2L, 5, 5)));

            var result = repository.findAllByPecaIds(Set.of(1L, 2L));

            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando nenhuma peça encontrada")
        void deveRetornarVazioQuandoNenhumaEncontrada() {
            when(jpaRepository.findAllByPecaIdIn(any())).thenReturn(List.of());

            var result = repository.findAllByPecaIds(Set.of(99L));

            assertThat(result).isEmpty();
        }
    }
}
