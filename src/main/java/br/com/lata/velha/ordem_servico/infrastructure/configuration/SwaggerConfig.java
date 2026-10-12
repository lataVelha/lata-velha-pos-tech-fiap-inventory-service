package br.com.lata.velha.ordem_servico.infrastructure.configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import br.com.lata.velha.shared.application.logging.Logger;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SwaggerConfig {

    private final Logger logger;

    @Bean
    public OpenAPI openAPI() {
        logger.logInfo("Configurando OpenAPI");
        return new OpenAPI()
                .info(new Info()
                        .title("Lata Velha — API de Estoque (Inventory)")
                        .description("""
                                API de gerenciamento de peças e estoque da oficina mecânica.

                                **Perfis:** USER / MECANICO / ADMIN consultam e movimentam estoque.

                                ---

                                **Endpoints:**
                                - `POST /pecas` — cadastra peça
                                - `GET /pecas`, `GET /pecas/{id}` — consulta peças
                                - `PUT /pecas/{id}` — atualiza peça
                                - `PATCH /pecas/{id}/desativar` — desativa peça
                                - `GET /pecas/{pecaId}/estoque` — consulta estoque
                                - `POST /pecas/{pecaId}/estoque/entrada` — registra entrada de estoque
                                - `POST /pecas/{pecaId}/estoque/saida` — registra saída de estoque
                                - `PATCH /pecas/{pecaId}/estoque/ajuste` — ajusta estoque

                                ---

                                **Integração com o OsService (general-flow.md):** a reserva de estoque na
                                aprovação de orçamento (`OrcamentoAprovado` → `InventoryChecked`) e a dedução
                                ao finalizar execução (`ServiceEnded`) hoje são expostas como casos de uso
                                prontos para um futuro consumidor de eventos — ver `ConferirEstoqueExecucaoUseCase`
                                e `DeduzirEstoqueExecucaoUseCase`.

                                ---

                                **Autenticação:** todos os endpoints exigem `Bearer <token>` — token emitido
                                pelo OsService via `POST /auth/login`.
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Equipe Lata Velha")
                        )
                )
                .components(new Components()
                        .addSecuritySchemes("bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT obtido via POST /auth/login")
                        )
                )
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
