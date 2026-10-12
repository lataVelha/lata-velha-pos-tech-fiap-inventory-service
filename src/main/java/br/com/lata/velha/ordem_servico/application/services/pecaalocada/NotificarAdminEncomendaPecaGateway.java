package br.com.lata.velha.ordem_servico.application.services.pecaalocada;

import br.com.lata.velha.ordem_servico.domain.entities.Peca;

import java.util.List;
import java.util.Optional;

public interface NotificarAdminEncomendaPecaGateway {
    Optional<Peca> findPecaPorId(Long id);

    // TODO: Inventory não tem dados de Funcionario/User — hoje lê uma lista estática de
    // e-mails via config (notificacoes.admin-emails). Um lookup real de admins por cargo
    // deve vir de uma chamada ao OsService, ou de um read-model sincronizado por evento.
    List<String> getAdminEmails();
}
