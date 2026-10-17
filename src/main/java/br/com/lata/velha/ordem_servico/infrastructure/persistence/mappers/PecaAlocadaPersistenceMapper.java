package br.com.lata.velha.ordem_servico.infrastructure.persistence.mappers;

import br.com.lata.velha.ordem_servico.domain.entities.PecaAlocada;
import br.com.lata.velha.ordem_servico.infrastructure.persistence.entities.PecaAlocadaEntity;
import org.springframework.stereotype.Component;

@Component
public class PecaAlocadaPersistenceMapper {

    public PecaAlocada toDomain(PecaAlocadaEntity entity) {
        return entity == null ? null : entity.toDomain();
    }

    public PecaAlocadaEntity toEntity(PecaAlocada domain) {
        return domain == null ? null : PecaAlocadaEntity.fromDomain(domain);
    }
}
