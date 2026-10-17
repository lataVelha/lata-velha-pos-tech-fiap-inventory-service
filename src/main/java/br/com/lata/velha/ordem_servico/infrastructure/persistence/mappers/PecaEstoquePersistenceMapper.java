package br.com.lata.velha.ordem_servico.infrastructure.persistence.mappers;

import br.com.lata.velha.ordem_servico.domain.entities.PecaEstoque;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaEstoqueEntity;
import org.springframework.stereotype.Component;

@Component
public class PecaEstoquePersistenceMapper {

    public PecaEstoque toDomain(PecaEstoqueEntity entity) {
        return entity == null ? null : entity.toDomain();
    }

    public PecaEstoqueEntity toEntity(PecaEstoque domain) {
        return domain == null ? null : PecaEstoqueEntity.fromDomain(domain);
    }
}
