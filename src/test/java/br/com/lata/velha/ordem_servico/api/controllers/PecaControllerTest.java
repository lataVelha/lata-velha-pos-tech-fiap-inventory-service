package br.com.lata.velha.ordem_servico.api.controllers;

import br.com.lata.velha.ordem_servico.infrastructure.security.config.SecurityConfig;
import br.com.lata.velha.ordem_servico.application.controllers.peca.PecaCleanController;
import br.com.lata.velha.ordem_servico.application.dtos.request.AtualizarPecaRequest;
import br.com.lata.velha.ordem_servico.application.dtos.request.CadastrarPecaRequest;
import br.com.lata.velha.ordem_servico.application.dtos.response.PecaResponse;
import br.com.lata.velha.shared.application.logging.Logger;
import br.com.lata.velha.shared.domain.pagination.PaginatedResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PecaController.class)
@Import(SecurityConfig.class)
class PecaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PecaCleanController cleanController;

    @MockBean
    private JwtDecoder jwtDecoder;

    @MockBean
    private JwtAuthenticationConverter jwtAuthenticationConverter;

    @MockBean
    private Logger logger;

    private PecaResponse buildResponse() {
        return new PecaResponse(1L, "Pastilha de Freio", "Pastilha traseira", new BigDecimal("45.90"), true);
    }

    private CadastrarPecaRequest buildRequest() {
        return new CadastrarPecaRequest("Pastilha de Freio", "Pastilha traseira", new BigDecimal("45.90"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /pecas deve retornar 201 e a peca criada")
    void shouldReturn201OnCreate() throws Exception {
        when(cleanController.cadastrar(any())).thenReturn(buildResponse());
        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(buildRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Pastilha de Freio"))
                .andExpect(jsonPath("$.ativo").value(true));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("POST /pecas com body invalido deve retornar 400")
    void shouldReturn400OnInvalidCreateRequest() throws Exception {
        var invalid = new CadastrarPecaRequest("", "", null);
        mockMvc.perform(post("/pecas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /pecas deve retornar 200 com lista paginada")
    void shouldReturn200OnList() throws Exception {
        var paginated = new PaginatedResult<>(List.of(buildResponse()), 0, 10, 1L, 1);
        when(cleanController.buscarTodos(0, 10)).thenReturn(paginated);
        mockMvc.perform(get("/pecas")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /pecas/{id} deve retornar 200 com a peca")
    void shouldReturn200OnFindById() throws Exception {
        when(cleanController.buscarPorId(1L)).thenReturn(buildResponse());
        mockMvc.perform(get("/pecas/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.nome").value("Pastilha de Freio"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /pecas/{id} deve retornar 200 com a peca atualizada")
    void shouldReturn200OnUpdate() throws Exception {
        var request = new AtualizarPecaRequest("Pastilha Dianteira", "Pastilha dianteira premium", new BigDecimal("65.00"));
        var updated = new PecaResponse(1L, "Pastilha Dianteira", "Pastilha dianteira premium", new BigDecimal("65.00"), true);
        when(cleanController.atualizar(eq(1L), any())).thenReturn(updated);
        mockMvc.perform(put("/pecas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Pastilha Dianteira"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PUT /pecas/{id} com body invalido deve retornar 400")
    void shouldReturn400OnInvalidUpdateRequest() throws Exception {
        var invalid = new AtualizarPecaRequest("", "", null);
        mockMvc.perform(put("/pecas/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("PATCH /pecas/{id}/desativar deve retornar 204")
    void shouldReturn204OnDesativar() throws Exception {
        doNothing().when(cleanController).desativar(1L);
        mockMvc.perform(patch("/pecas/1/desativar"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /pecas sem autenticacao deve retornar 401")
    void shouldReturn401WhenUnauthenticated() throws Exception {
        mockMvc.perform(get("/pecas"))
                .andExpect(status().isUnauthorized());
    }
}
