package br.com.lata.velha.ordem_servico.infrastructure.persistence.gateways;

import br.com.lata.velha.ordem_servico.application.use_cases.peca.*;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaalocada.*;
import br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque.*;
import br.com.lata.velha.ordem_servico.domain.entities.*;
import br.com.lata.velha.ordem_servico.domain.repositories.*;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.InvalidDataAccessApiUsageException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PecaGatewayImpl implements
        CadastrarPecaGateway,
        AtualizarPecaGateway,
        BuscarPecaPorIdGateway,
        BuscarPecasGateway,
        DesativarPecaGateway,
        BuscarPecaAlocadaPorIdGateway,
        BuscarPecasAlocadasGateway,
        AjustarPecaEstoqueGateway,
        BuscarPecaEstoqueGateway,
        EntradaPecaEstoqueGateway,
        SaidaPecaEstoqueGateway,
        ConferirEstoqueGateway,
        DeduzirEstoqueGateway,
        ReceberOrcamentoAprovadoGateway,
        ReceberServiceEndedGateway {

    private final PecaRepository pecaRepository;
    private final PecaEstoqueRepository pecaEstoqueRepository;
    private final PecaAlocadaRepository pecaAlocadaRepository;

    @Override
    public Peca salvarPeca(Peca peca) {
        return pecaRepository.save(peca);
    }

    @Override
    public PecaEstoque salvarEstoque(PecaEstoque estoque) {
        return pecaEstoqueRepository.save(estoque);
    }

    @Override
    public Peca getPecaAtivaPorId(Long id) {
        return pecaRepository.getActiveById(id);
    }

    @Override
    public PaginatedResult<Peca> findAll(int page, int size) {
        return pecaRepository.findAllActivePaginated(page, size);
    }

    @Override
    public PecaAlocada getPecaAlocadaPorId(Long id) {
        return pecaAlocadaRepository.findById(id);
    }

    @Override
    public PaginatedResult<PecaAlocada> findByExecucaoServicoId(Long execucaoServicoId, int page, int size) {
        return pecaAlocadaRepository.findByServicoOsId(execucaoServicoId, page, size);
    }

    @Override
    public PecaEstoque getEstoquePorPecaId(Long pecaId) {
        return pecaEstoqueRepository.findByPecaId(pecaId)
                .orElse(new PecaEstoque(pecaId, 0, 0));
    }

    @Override
    public PecaAlocada salvarPecaAlocada(PecaAlocada alocada) {
        return pecaAlocadaRepository.save(alocada);
    }

    @Override
    public PecaAlocada getOuCriarPecaAlocada(Long execucaoServicoId, Long pecaId, Integer quantidadeSolicitada, BigDecimal valorUnitarioPeca) {
        try {
            return pecaAlocadaRepository.findByPecaIdAndServicoOsId(pecaId, execucaoServicoId);
        } catch (IllegalArgumentException | InvalidDataAccessApiUsageException e) {
            return PecaAlocada.create(pecaId, execucaoServicoId, valorUnitarioPeca, quantidadeSolicitada);
        }
    }

    @Override
    public List<PecaEstoque> getEstoquePorPecaIds(Set<Long> pecaIds) {
        return pecaEstoqueRepository.findAllByPecaIds(pecaIds);
    }

    @Override
    public void salvarEstoques(Collection<PecaEstoque> estoques) {
        pecaEstoqueRepository.saveAll(estoques);
    }

    @Override
    public Optional<PecaAlocada> buscarPecaAlocadaExistente(Long execucaoServicoId, Long pecaId) {
        try {
            return Optional.of(pecaAlocadaRepository.findByPecaIdAndServicoOsId(pecaId, execucaoServicoId));
        } catch (IllegalArgumentException | InvalidDataAccessApiUsageException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Peca> getPecasPorIds(Set<Long> ids) {
        return pecaRepository.findAllByIds(ids);
    }

    @Override
    public List<PecaAlocada> getTodasPecaAlocadaPorExecucaoServicoId(Long execucaoServicoId) {
        return pecaAlocadaRepository.findAllByExecucaoServicoId(execucaoServicoId);
    }

    @Override
    public List<PecaAlocada> buscarPendentesPorPeca(Long pecaId) {
        return pecaAlocadaRepository.buscarPendentesPorPecaOrdenado(pecaId);
    }
}
