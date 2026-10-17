package br.com.lata.velha.ordem_servico.infrastructure.persistence.repositories;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaAlocadaEntity;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PecaAlocadaRepositoryImplTest {

    @Mock
    private PecaAlocadaJpaRepository jpaRepository;

    @Mock
    private Logger logger;

    @InjectMocks
    private PecaAlocadaRepositoryImpl repository;

    private PecaAlocada domainPeca() {
        return new PecaAlocada(1L, 10L, 99L, new BigDecimal("10.00"), 3, 0, 0, 0, StatusPecaAlocada.PENDENTE, LocalDateTime.now());
    }

    private PecaAlocadaEntity entityPeca() {
        return new PecaAlocadaEntity(1L, 10L, 99L, new BigDecimal("10.00"), 3, 0, 0, 0, StatusPecaAlocada.PENDENTE, LocalDateTime.now());
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("deve salvar e retornar domínio mapeado")
        void deveSalvarERetornarDominio() {
            PecaAlocadaEntity saved = entityPeca();
            when(jpaRepository.save(any())).thenReturn(saved);

            PecaAlocada result = repository.save(domainPeca());

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getPecaId()).isEqualTo(10L);
            assertThat(result.getExecucaoServicoId()).isEqualTo(99L);
            assertThat(result.getQuantidadeSolicitada()).isEqualTo(3);
            assertThat(result.getStatus()).isEqualTo(StatusPecaAlocada.PENDENTE);
            verify(jpaRepository).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("deve retornar domínio quando encontrado")
        void deveRetornarQuandoEncontrado() {
            when(jpaRepository.findById(1L)).thenReturn(Optional.of(entityPeca()));

            PecaAlocada result = repository.findById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(jpaRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> repository.findById(99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Peça alocada não encontrada");
        }
    }

    @Nested
    @DisplayName("findByServicoOsId")
    class FindByServicoOsId {

        @Test
        @DisplayName("deve retornar PaginatedResult com registros")
        void deveRetornarPaginatedResult() {
            var entity = entityPeca();
            var page = new PageImpl<>(List.of(entity), PageRequest.of(0, 10), 1);
            when(jpaRepository.findByExecucaoServicoId(eq(99L), any())).thenReturn(page);

            var result = repository.findByServicoOsId(99L, 0, 10);

            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
            assertThat(result.page()).isZero();
            assertThat(result.size()).isEqualTo(10);
        }

        @Test
        @DisplayName("deve retornar PaginatedResult vazio quando não há registros")
        void deveRetornarVazioQuandoSemRegistros() {
            var page = new PageImpl<PecaAlocadaEntity>(List.of(), PageRequest.of(0, 10), 0);
            when(jpaRepository.findByExecucaoServicoId(eq(99L), any())).thenReturn(page);

            var result = repository.findByServicoOsId(99L, 0, 10);

            assertThat(result.content()).isEmpty();
            assertThat(result.totalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("deve chamar deleteById no JPA repository")
        void deveChamarDeleteById() {
            repository.delete(1L);

            verify(jpaRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("buscarPendentesPorPecaOrdenado")
    class BuscarPendentesPorPecaOrdenado {

        @Test
        @DisplayName("deve retornar lista de peças pendentes")
        void deveRetornarPecasPendentes() {
            var entity = new PecaAlocadaEntity(2L, 10L, 99L, new BigDecimal("10.00"), 5, 2, 3, 0, StatusPecaAlocada.PARCIAL, LocalDateTime.now());
            when(jpaRepository.buscarPendentesPorPecaOrdenado(10L)).thenReturn(List.of(entity));

            List<PecaAlocada> result = repository.buscarPendentesPorPecaOrdenado(10L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(StatusPecaAlocada.PARCIAL);
        }

        @Test
        @DisplayName("deve retornar lista vazia quando não há pendentes")
        void deveRetornarListaVaziaQuandoNaoHaPendentes() {
            when(jpaRepository.buscarPendentesPorPecaOrdenado(10L)).thenReturn(List.of());

            List<PecaAlocada> result = repository.buscarPendentesPorPecaOrdenado(10L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByPecaIdAndServicoOsId")
    class FindByPecaIdAndServicoOsId {

        @Test
        @DisplayName("deve retornar domínio quando encontrado")
        void deveRetornarQuandoEncontrado() {
            when(jpaRepository.findByPecaIdAndExecucaoServicoId(10L, 99L))
                    .thenReturn(Optional.of(entityPeca()));

            PecaAlocada result = repository.findByPecaIdAndServicoOsId(10L, 99L);

            assertThat(result.getPecaId()).isEqualTo(10L);
            assertThat(result.getExecucaoServicoId()).isEqualTo(99L);
        }

        @Test
        @DisplayName("deve lançar IllegalArgumentException quando não encontrado")
        void deveLancarExcecaoQuandoNaoEncontrado() {
            when(jpaRepository.findByPecaIdAndExecucaoServicoId(10L, 99L))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> repository.findByPecaIdAndServicoOsId(10L, 99L))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Peça alocada não encontrada");
        }
    }
}
