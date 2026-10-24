package br.com.lata.velha.ordem_servico.application.use_cases.pecaestoque;

import br.com.lata.velha.ordem_servico.application.dtos.request.MovimentarPecaEstoqueRequest;
import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEntity;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEstoqueEntity;
import br.com.lata.velha.shared.application.gateways.EventPublisher;
import br.com.lata.velha.shared.application.logging.Logger;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.datasource.url=jdbc:h2:mem:entrada-peca-estoque-uc-it;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DEFAULT_NULL_ORDERING=HIGH"
})
@Transactional
class EntradaPecaEstoqueUseCaseIT {

    @Autowired private EntradaPecaEstoqueGateway gateway;
    @Autowired private EntityManager em;
    @Autowired private Logger logger;

    @MockBean private EventPublisher eventPublisher;

    private EntradaPecaEstoqueUseCase useCase;

    private Long pecaId;

    @BeforeEach
    void setUp() {
        useCase = new EntradaPecaEstoqueUseCase(gateway, eventPublisher, logger);

        PecaEntity peca = new PecaEntity();
        peca.setNome("Filtro de óleo");
        peca.setDescricao("Filtro premium");
        peca.setValor(new BigDecimal("35.00"));
        em.persist(peca);
        pecaId = peca.getId();

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("deve criar estoque e persistir quando não existe")
    void deveCriarEstoqueQuandoNaoExiste() {
        PecaEstoque response = useCase.execute(pecaId, new MovimentarPecaEstoqueRequest(10));

        assertThat(response.getQuantidadeArmazenada()).isEqualTo(10);
        assertThat(response.getQuantidadeDisponivel()).isEqualTo(10);

        PecaEstoqueEntity estoque = em.find(PecaEstoqueEntity.class, pecaId);
        assertThat(estoque).isNotNull();
        assertThat(estoque.getQuantidadeArmazenada()).isEqualTo(10);
        assertThat(estoque.getQuantidadeDisponivel()).isEqualTo(10);
    }

    @Test
    @DisplayName("deve somar ao estoque existente")
    void deveSomarAoEstoqueExistente() {
        PecaEstoqueEntity existente = new PecaEstoqueEntity(pecaId, 5, 5);
        em.persist(existente);
        em.flush();
        em.clear();

        PecaEstoque response = useCase.execute(pecaId, new MovimentarPecaEstoqueRequest(3));

        assertThat(response.getQuantidadeArmazenada()).isEqualTo(8);
        assertThat(response.getQuantidadeDisponivel()).isEqualTo(8);
    }

    @Test
    @DisplayName("deve lançar exceção quando peça não existe")
    void deveLancarExcecaoQuandoPecaNaoExiste() {
        Long idInexistente = 9999L;
        var request = new MovimentarPecaEstoqueRequest(5);

        assertThatThrownBy(() -> useCase.execute(idInexistente, request))
                .isInstanceOf(RuntimeException.class);
    }
}
