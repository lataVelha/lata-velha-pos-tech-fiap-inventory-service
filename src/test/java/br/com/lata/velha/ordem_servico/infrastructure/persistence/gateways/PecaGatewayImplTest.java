package br.com.lata.velha.ordem_servico.infrastructure.persistence.gateways;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.ordem_servico.domain.enums.StatusPecaAlocada;
import br.com.lata.velha.ordem_servico.domain.repositories.PecaAlocadaRepository;
import br.com.lata.velha.ordem_servico.domain.repositories.PecaEstoqueRepository;
import br.com.lata.velha.ordem_servico.domain.repositories.PecaRepository;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.InvalidDataAccessApiUsageException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PecaGatewayImplTest {

    private static final String PECA_ID = "10";
    private static final Long EXECUCAO_ID = 99L;

    @Mock
    private PecaRepository pecaRepository;

    @Mock
    private PecaEstoqueRepository pecaEstoqueRepository;

    @Mock
    private PecaAlocadaRepository pecaAlocadaRepository;

    @InjectMocks
    private PecaGatewayImpl gateway;

    private final Peca peca = new Peca(PECA_ID, "Filtro", "Filtro de óleo", new BigDecimal("35.00"));

    private PecaAlocada alocada() {
        return new PecaAlocada("1", PECA_ID, EXECUCAO_ID, new BigDecimal("35.00"),
                2, 2, 0, 0, StatusPecaAlocada.RESERVADA, LocalDateTime.now());
    }

    @Test
    @DisplayName("deve delegar operações de peça ao repositório")
    void deveDelegarOperacoesDePeca() {
        var paginado = new PaginatedResult<>(List.of(peca), 0, 10, 1, 1);
        when(pecaRepository.save(peca)).thenReturn(peca);
        when(pecaRepository.getActiveById(PECA_ID)).thenReturn(peca);
        when(pecaRepository.findAllActivePaginated(0, 10)).thenReturn(paginado);
        when(pecaRepository.findAllByIds(Set.of(PECA_ID))).thenReturn(List.of(peca));

        assertThat(gateway.salvarPeca(peca)).isEqualTo(peca);
        assertThat(gateway.getPecaAtivaPorId(PECA_ID)).isEqualTo(peca);
        assertThat(gateway.findAll(0, 10)).isEqualTo(paginado);
        assertThat(gateway.getPecasPorIds(Set.of(PECA_ID))).containsExactly(peca);
    }

    @Test
    @DisplayName("deve delegar operações de estoque ao repositório")
    void deveDelegarOperacoesDeEstoque() {
        var estoque = new PecaEstoque(PECA_ID, 10, 8);
        when(pecaEstoqueRepository.save(estoque)).thenReturn(estoque);
        when(pecaEstoqueRepository.findByPecaId(PECA_ID)).thenReturn(Optional.of(estoque));
        when(pecaEstoqueRepository.findAllByPecaIds(Set.of(PECA_ID))).thenReturn(List.of(estoque));

        assertThat(gateway.salvarEstoque(estoque)).isEqualTo(estoque);
        assertThat(gateway.getEstoquePorPecaId(PECA_ID)).isEqualTo(estoque);
        assertThat(gateway.getEstoquePorPecaIds(Set.of(PECA_ID))).containsExactly(estoque);

        Collection<PecaEstoque> estoques = List.of(estoque);
        gateway.salvarEstoques(estoques);

        verify(pecaEstoqueRepository).saveAll(estoques);
    }

    @Test
    @DisplayName("deve criar estoque zerado quando não existe registro")
    void deveCriarEstoqueZeradoQuandoAusente() {
        when(pecaEstoqueRepository.findByPecaId(PECA_ID)).thenReturn(Optional.empty());

        var estoque = gateway.getEstoquePorPecaId(PECA_ID);

        assertThat(estoque.getPecaId()).isEqualTo(PECA_ID);
        assertThat(estoque.getQuantidadeArmazenada()).isZero();
        assertThat(estoque.getQuantidadeDisponivel()).isZero();
    }

    @Test
    @DisplayName("deve delegar operações de peça alocada ao repositório")
    void deveDelegarOperacoesDePecaAlocada() {
        var alocada = alocada();
        var paginado = new PaginatedResult<>(List.of(alocada), 0, 10, 1, 1);
        when(pecaAlocadaRepository.save(alocada)).thenReturn(alocada);
        when(pecaAlocadaRepository.findById("1")).thenReturn(alocada);
        when(pecaAlocadaRepository.findByServicoOsId(EXECUCAO_ID, 0, 10)).thenReturn(paginado);
        when(pecaAlocadaRepository.findAllByExecucaoServicoId(EXECUCAO_ID)).thenReturn(List.of(alocada));
        when(pecaAlocadaRepository.buscarPendentesPorPecaOrdenado(PECA_ID)).thenReturn(List.of(alocada));

        assertThat(gateway.salvarPecaAlocada(alocada)).isEqualTo(alocada);
        assertThat(gateway.getPecaAlocadaPorId("1")).isEqualTo(alocada);
        assertThat(gateway.findByExecucaoServicoId(EXECUCAO_ID, 0, 10)).isEqualTo(paginado);
        assertThat(gateway.getTodasPecaAlocadaPorExecucaoServicoId(EXECUCAO_ID)).containsExactly(alocada);
        assertThat(gateway.buscarPendentesPorPeca(PECA_ID)).containsExactly(alocada);
    }

    @Test
    @DisplayName("getOuCriarPecaAlocada deve retornar a existente quando encontrada")
    void deveRetornarPecaAlocadaExistente() {
        var alocada = alocada();
        when(pecaAlocadaRepository.findByPecaIdAndServicoOsId(PECA_ID, EXECUCAO_ID)).thenReturn(alocada);

        var result = gateway.getOuCriarPecaAlocada(EXECUCAO_ID, PECA_ID, 2, new BigDecimal("35.00"));

        assertThat(result).isSameAs(alocada);
    }

    @Test
    @DisplayName("getOuCriarPecaAlocada deve criar quando o repositório lança IllegalArgumentException")
    void deveCriarPecaAlocadaQuandoIllegalArgument() {
        when(pecaAlocadaRepository.findByPecaIdAndServicoOsId(PECA_ID, EXECUCAO_ID))
                .thenThrow(new IllegalArgumentException("id inválido"));

        var result = gateway.getOuCriarPecaAlocada(EXECUCAO_ID, PECA_ID, 2, new BigDecimal("35.00"));

        assertThat(result.getId()).isNull();
        assertThat(result.getPecaId()).isEqualTo(PECA_ID);
        assertThat(result.getExecucaoServicoId()).isEqualTo(EXECUCAO_ID);
        assertThat(result.getQuantidadeSolicitada()).isEqualTo(2);
    }

    @Test
    @DisplayName("getOuCriarPecaAlocada deve criar quando o repositório lança erro de API")
    void deveCriarPecaAlocadaQuandoErroDeApi() {
        when(pecaAlocadaRepository.findByPecaIdAndServicoOsId(PECA_ID, EXECUCAO_ID))
                .thenThrow(new InvalidDataAccessApiUsageException("consulta inválida"));

        var result = gateway.getOuCriarPecaAlocada(EXECUCAO_ID, PECA_ID, 3, BigDecimal.ONE);

        assertThat(result.getId()).isNull();
        assertThat(result.getQuantidadeSolicitada()).isEqualTo(3);
    }

    @Test
    @DisplayName("buscarPecaAlocadaExistente deve retornar vazio quando não encontrada")
    void deveRetornarVazioQuandoNaoEncontrada() {
        when(pecaAlocadaRepository.findByPecaIdAndServicoOsId(PECA_ID, EXECUCAO_ID))
                .thenThrow(new IllegalArgumentException("id inválido"));

        assertThat(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, PECA_ID)).isEmpty();
    }

    @Test
    @DisplayName("buscarPecaAlocadaExistente deve retornar vazio quando há erro de API")
    void deveRetornarVazioQuandoErroDeApi() {
        when(pecaAlocadaRepository.findByPecaIdAndServicoOsId(PECA_ID, EXECUCAO_ID))
                .thenThrow(new InvalidDataAccessApiUsageException("consulta inválida"));

        assertThat(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, PECA_ID)).isEmpty();
    }

    @Test
    @DisplayName("buscarPecaAlocadaExistente deve retornar a peça quando encontrada")
    void deveRetornarPecaAlocadaQuandoEncontrada() {
        var alocada = alocada();
        when(pecaAlocadaRepository.findByPecaIdAndServicoOsId(PECA_ID, EXECUCAO_ID)).thenReturn(alocada);

        assertThat(gateway.buscarPecaAlocadaExistente(EXECUCAO_ID, PECA_ID)).contains(alocada);
    }

}
