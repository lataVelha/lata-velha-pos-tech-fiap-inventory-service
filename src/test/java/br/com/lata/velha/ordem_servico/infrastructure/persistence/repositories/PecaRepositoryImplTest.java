package br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEntity;
import br.com.lata.velha.shared.application.logging.Logger;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PecaRepositoryImplTest {

    @Mock
    private PecaJpaRepository jpaRepository;

    @Mock
    private Logger logger;

    @InjectMocks
    private PecaRepositoryImpl repository;

    private Peca domainPeca() {
        return new Peca(1L, "Filtro de óleo", "Filtro", new BigDecimal("50.00"));
    }

    private PecaEntity entityPeca() {
        var e = new PecaEntity();
        e.setId(1L);
        e.setNome("Filtro de óleo");
        e.setDescricao("Filtro");
        e.setValor(new BigDecimal("50.00"));
        e.setAtivo(true);
        return e;
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("deve salvar e retornar domínio mapeado")
        void deveSalvarERetornarDominio() {
            PecaEntity entity = entityPeca();
            when(jpaRepository.save(entity)).thenReturn(entity);

            Peca result = repository.save(domainPeca());

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getNome()).isEqualTo("Filtro de óleo");
            verify(jpaRepository).save(entity);
        }
    }

    @Nested
    @DisplayName("getActiveById")
    class GetActiveById {

        @Test
        @DisplayName("deve retornar domínio quando encontrado")
        void deveRetornarQuandoEncontrado() {
            PecaEntity entity = entityPeca();
            when(jpaRepository.findByIdAndAtivoTrue(1L)).thenReturn(Optional.of(entity));

            Peca result = repository.getActiveById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(jpaRepository.findByIdAndAtivoTrue(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> repository.getActiveById(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Peça não encontrada");
        }
    }

    @Nested
    @DisplayName("findAllActivePaginated")
    class FindAllActivePaginated {

        @Test
        @DisplayName("deve retornar PaginatedResult com registros")
        void deveRetornarPaginatedResult() {
            PecaEntity entity = entityPeca();
            var page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
            when(jpaRepository.findByAtivoTrue(any())).thenReturn(page);

            var result = repository.findAllActivePaginated(0, 10);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("deve retornar PaginatedResult vazio quando não há registros")
        void deveRetornarVazioQuandoSemRegistros() {
            var page = new PageImpl<PecaEntity>(List.of(), PageRequest.of(0, 10), 0);
            when(jpaRepository.findByAtivoTrue(any())).thenReturn(page);

            var result = repository.findAllActivePaginated(0, 10);

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("existsActiveById")
    class ExistsActiveById {

        @Test
        @DisplayName("deve retornar true quando peça ativa existe")
        void deveRetornarTrueQuandoExiste() {
            when(jpaRepository.existsByIdAndAtivoTrue(1L)).thenReturn(true);

            assertThat(repository.existsActiveById(1L)).isTrue();
        }

        @Test
        @DisplayName("deve retornar false quando peça não existe ou está inativa")
        void deveRetornarFalseQuandoNaoExiste() {
            when(jpaRepository.existsByIdAndAtivoTrue(99L)).thenReturn(false);

            assertThat(repository.existsActiveById(99L)).isFalse();
        }
    }
}
