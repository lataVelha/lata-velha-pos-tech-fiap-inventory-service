package br.com.lata.velha.ordem_servico.domain.exceptions.not_found_exceptions;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;
import br.com.lata.velha.shared.domain.exceptions.NotFoundException;

public final class PecaNotFoundException extends NotFoundException {
    private PecaNotFoundException(String param, String value) {
        super(Peca.class, param, value);
    }

    public static PecaNotFoundException fromId(Long value) {
        return new PecaNotFoundException("id", value.toString());
    }
}
