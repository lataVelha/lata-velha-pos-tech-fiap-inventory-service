package br.com.lata.velha.infrastructure.configuration;

import io.mongock.runner.springboot.EnableMongock;
import org.springframework.context.annotation.Configuration;

/**
 * Liga o Mongock (migrations do MongoDB).
 *
 * O `mongock-springboot-v3` 5.x NÃO tem auto-configuração — sem esta anotação
 * o `mongock.migration-scan-package` do application.yaml é ignorado e as
 * migrations nunca rodam. Sintoma: banco vazio (sem collections, sem índices
 * e sem a carga inicial de peças do V002), e o inventory responde 200 com
 * lista vazia em GET /pecas.
 */
@Configuration
@EnableMongock
public class MongockConfig {
}
