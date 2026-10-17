
---

## Desenvolvimento

Este documento contém algumas diretrizes de desenvolvimento acordadas pelos devs


### Idioma

- Métodos em **inglês** (`execute()`, `findByCpf()`, `build()`)
- Domínios e entidades em **português** (`Veiculo`, `Proprietario`, `placa`)
- Mensagens de erro em **português**

### Injeção de Dependência

Use **EXCLUSIVAMENTE** `@RequiredArgsConstructor` do Lombok. Nada de `@Autowired` em campos, nada de constructores manuais.

### DTOs

Use **`Record`** nativos do Java 17+:
```java
public record CriarProprietarioRequest(
    @NotBlank String nome,
    @NotBlank @Email String email,
    String documento
) {}
```

### Testes

Obrigatório: Unitários + Integração para Use Cases.  
Ecossistema: JUnit 5, Mockito, JaCoCo.  
Cobertura mínima: 80%.

Padrão de nomes:
- `*Test.java` → unitários
- `*IT.java` → integração (com Spring Context + H2)