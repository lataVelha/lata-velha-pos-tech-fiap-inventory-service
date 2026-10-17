<h1 align="center">🚗 Lata Velha</h1>

<h3 align="center">Pós-Tech FIAP — Arquitetura de Software</h3>

<p align="center">
  Sistema de gestão automotiva com DDD
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk&logoColor=white" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-green?logo=springboot&logoColor=white" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/PostgreSQL-15-blue?logo=postgresql&logoColor=white" alt="PostgreSQL"/>
  <img src="https://img.shields.io/badge/Docker-Compose-blue?logo=docker&logoColor=white" alt="Docker"/>
  <img src="https://img.shields.io/badge/SonarQube-Community-4E9BCD?logo=sonarqube&logoColor=white" alt="SonarQube"/>
  <img src="https://img.shields.io/badge/JaCoCo-Coverage-red?logo=java&logoColor=white" alt="JaCoCo"/>
</p>

---

## Sumário

### Fase 1 — Sistema de gestão (DDD + Docker Compose)

- [Arquitetura (camadas DDD)](#arquitetura)
- [Pré-requisitos](#pré-requisitos)
- [Como rodar (Docker Compose)](#como-rodar)
- [Configurações por Contexto](#configurações-por-contexto)
- [Autenticação](#autenticação)
- [Observabilidade](#observabilidade)
- [Testes](#testes)
- [SonarQube](#sonarqube)
- [Tecnologias](#tecnologias)

### Fase 2 — Infraestrutura cloud (K8s + Terraform + CI/CD)

- [Objetivos e componentes](#fase-2--tech-challenge)
- [Desenho da arquitetura AWS](./documentation/arquitetura-aws.svg)
- [Fluxo de deploy (CI/CD)](./documentation/pipeline-cicd.svg)
- [Deploy da aplicação](./terraform/README.md) — este repo só aplica os recursos da aplicação no cluster
- Infraestrutura base (VPC/EKS/ECR/ALB): repo [`infra`](https://github.com/lataVelha/lata-velha-pos-tech-fiap-infra)
- Banco de dados (RDS): repo [`infra-db`](https://github.com/lataVelha/lata-velha-pos-tech-fiap-infra-db)

---

## Fase 2 — Tech Challenge

Evolução da Fase 1. Provisionamento, deploy e operação migrados para AWS com Terraform e GitHub Actions.

### Objetivos

- **Reduzir riscos operacionais** com infraestrutura escalável na AWS (EKS + RDS + ALB).
- **Automatizar provisionamento e deploy** com Terraform e GitHub Actions.
- **Sustentar a evolução do código** com Clean Architecture, testes automatizados e cobertura mínima de 80%.
- **Absorver picos de demanda** com autoscaling em duas camadas (HPA de pods + Cluster Autoscaler de nodes).

### Componentes da aplicação

Refatoração para arquitetura em camadas com DDD: `domain` puro (zero framework), `application` com casos de uso, `infrastructure` com adapters (JPA, JWT, SMTP), `presentation` com controllers.

Endpoints novos da fase:

- Abertura de OS
- Consulta de status da OS
- Aprovação externa de orçamento
- Listagem de OS ordenada por status (Em Execução > Aguardando Aprovação > Diagnóstico > Recebida, mais antigas primeiro)
- Atualização de status via e-mail

### Desenho da arquitetura

<p align="center">
  <img src="./documentation/arquitetura-aws.svg" alt="Arquitetura AWS" width="700"/>
</p>

<p align="center">
  <img src="./documentation/pipeline-cicd.svg" alt="Pipeline CI/CD" width="700"/>
</p>

### Provisionamento, deploy e operação

A infraestrutura foi dividida em quatro repositórios, cada um com seu próprio state e
pipeline de CI/CD. Este repo (`app`) aplica o deploy k8s da aplicação e anexa a própria
integração com o ALB + rotas no API Gateway compartilhado — detalhes em
**[`terraform/README.md`](./terraform/README.md)**.

#### Dependências

- [`infra`](https://github.com/lataVelha/lata-velha-pos-tech-fiap-infra) — VPC, EKS, ECR, ALB
  interno, "casco" do API Gateway, Cluster Autoscaler
- [`infra-db`](https://github.com/lataVelha/lata-velha-pos-tech-fiap-infra-db) — RDS PostgreSQL
- [`lambda`](https://github.com/lataVelha/lata-velha-pos-tech-fiap-lambda) — login por CPF e a
  lambda authorizer usada nas rotas protegidas

O ALB não é acessível diretamente (subnets privadas, sem IP público) — todo tráfego externo
passa pelo **API Gateway** (repo `infra`).

### Links da fase

- **Collection de APIs (Swagger local, via `docker compose up`):** http://localhost:8080/swagger-ui.html
- **Collection de APIs (AWS):** URL do API Gateway (`app_api_endpoint`, output do `infra` addons) + `/swagger-ui.html`
- **Vídeo demonstrativo (≤15 min):**

---

## Fase 1 — Tech Challenge

## Arquitetura

O projeto segue **Domain-Driven Design (DDD)** com arquitetura em camadas, onde cada camada possui responsabilidades bem definidas e as dependências sempre apontam para o centro (domínio).

<p align="center">
  <img src="./documentation/arquitetura-ddd-lata-velha.svg" alt="Arquitetura DDD em camadas" width="700"/>
</p>

```
br.com.lata.velha
├── api/                 → Entrada HTTP (controllers, exception handlers)
├── application/         → Casos de uso, DTOs e gateways (interfaces para serviços externos)
├── domain/              → Regras de negócio puras (zero frameworks)
└── infrastructure/      → JPA, JWT, configs do Spring
```

**Regra principal:** o `domain` não importa nenhuma outra camada. A `infrastructure` implementa as interfaces definidas pelo `domain` e `application`, conectadas via injeção de dependência do Spring.

### Estrutura de Contextos

O código é dividido em 3 contextos bem separados:

<p align="center">
  <img src="./documentation/arquitetura-contextos-lata-velha.svg" alt="Arquitetura com contextos" width="700"/>
</p>

**Authentication**

- Só cuida de login e gerar token JWT
- Basicamente: usuário entra com email/senha e recebe um token que é válido por 1 hora
- Fica em `authentication/`, totalmente isolado do restante do código

**Ordem de Serviço**

- Aqui roda o core business da oficina: proprietários, veículos, serviços, peças, ordens de serviço
- Uma OS passa por diferentes estados quando criada: recebida → diagnóstico → aguardando aprovação → aprovada → execução → finalizada → entregue
- Se o proprietário recusar todos os serviços identificados, a Ordem de Serviço é reprovada, liberando a retirada do veículo
- Fica tudo em `ordem_servico/`

**Shared**

- Código que qualquer contexto precisa: exceções, validadores, tipos básicos que se repetem

### Notificações por E-mail

O sistema dispara e-mails em 3 momentos do fluxo:

| Evento                                       | Destinatário            | Template                     |
| -------------------------------------------- | ----------------------- | ---------------------------- |
| Mudança de status da OS (7 transições)       | Proprietário do veículo | `os-notificacao.html`        |
| Cadastro de novo proprietário                | Proprietário            | `proprietario-cadastro.html` |
| Aprovação de OS com peça em falta no estoque | Todos os admins         | `peca-encomenda.html`        |

A implementação segue **Ports & Adapters**: a camada `domain` não conhece SMTP, e a `application` depende apenas das interfaces `EmailProvider` e `EmailTemplateProvider`. O adapter `GmailEmailProvider` (SMTP via `JavaMailSender`) é `@Async` — falha no envio **nunca quebra a request HTTP**.

Configuração via env vars:

```bash
SPRING_MAIL_USERNAME=seu@gmail.com
SPRING_MAIL_PASSWORD=xxxx xxxx xxxx xxxx   # Senha de App do Google
```

> Detalhes completos (arquitetura, regras de implementação, checklist de PR) em [`guias-desenvolvimento.md`](./guias-desenvolvimento.md#envio-de-e-mail).

## Pré-requisitos

- **Java 21** — verificar no terminal com `java -version`
- **Docker** instalado e rodando — testar no terminal com `docker ps`
- **Git** pra clonar o projeto

---

## Como rodar

### Infraestrutura

O arquivo principal traz PostgreSQL + aplicação:

```bash
docker compose up --build -d
```

Se necessário acesso ao banco com PgAdmin e SonarQube, rode:

```bash
docker compose -f docker/docker-compose-dev.yml up -d
```

(PgAdmin fica em http://localhost:5050, email `admin@admin.com` / senha `admin123`)

Verificar se está tudo rodando:

```bash
docker ps
```

### Usando a API

Abra o Swagger em http://localhost:8080/swagger-ui.html

Pra acessar os endpoints protegidos, é necessário fazer login primeiro:

1. Procure a seção **Authentication**
2. Clique em `POST /auth/login`
3. Passe `admin@latavelha.com` como login e `Admin@123` como senha
4. Copie o token que é retornado
5. Clique no botão **Authorize** (canto direito em cima)
6. Cole o token (sem prefixo `Bearer`) e confirme
7. Pronto, todos os endpoints autenticados funcionam

Para parar tudo:

```bash
docker compose down -v
```

(o `-v` limpa os volumes, apagando dados do banco e SonarQube)

E pgAdmin e Sonar:

```bash
docker compose -f docker/docker-compose-dev.yml down -v
```

---

## Configurações por Contexto

**Desenvolvimento local:**

- Banco: PostgreSQL em localhost:5432
- App: http://localhost:8080
- Sem profile especial — pega a `application.yaml` default

**Via Docker:**

- App roda num container Docker
- Seta `ENVIRONMENT=docker` automaticamente
- Banco fica em `postgres:5432` (nome do hostname no Docker)
- App na porta 8080, SonarQube na 9000

**Durante testes:**

- BD em memória (H2)
- Profile `test` ativa automaticamente
- Migrations desligadas (schema é criado fresh cada vez)

**Credenciais padrão (de desenvolvimento):**

- Host BD: `localhost` (ou `postgres` se Docker)
- Porta: 5432
- Database: `lata_velha`
- Usuário: `admin`
- Senha: `admin123`

### Soft-Delete

Todas as entidades importantes têm uma coluna `ATIVO` (true/false). Quando você deleta algo, na verdade é só marcado como `ATIVO = false`. Quando lista, só aparecem os ativos.

Use `PATCH /recurso/{id}/desativar` pra inativar e `PATCH /recurso/{id}/reativar` pra ativar novamente.

---

## Autenticação

O sistema usa JWT com RSA (chaves de 2048 bits). Basicamente: você faz login, recebe um token, envia o token em toda requisição protegida.

**Detalhes do token:**

- Válido por 1 hora (3600 segundos)
- Assinado com chave privada RSA (em `app.key`)
- Quando o servidor vê o token, valida com a chave pública (em `app.pub`)
- Header esperado: `Authorization: Bearer <token>`

**Usuários de teste (só desenvolvimento):**

- `admin@latavelha.com` / `Admin@123` — acesso total
- `atendente@latavelha.com` / `Atend@123` — pode abrir OS, aprovar, entregar
- `mecanico@latavelha.com` / `Mecan@123` — pode fazer diagnóstico e executar serviços

**Login por CPF (via API Gateway, ambiente AWS):** além de `POST /auth/login` (email/senha),
existe um segundo caminho de entrada — `POST /auth/cpf`, servido por uma lambda do repo
[`lambda`](https://github.com/lataVelha/lata-velha-pos-tech-fiap-lambda), fora deste repo. Exige
CPF **e** senha (`{ "cpf": "...", "password": "..." }`) — a mesma senha do login por email,
conferida contra o mesmo hash BCrypt (`USERS.CREDENTIAL`). Emite o mesmo tipo de token JWT (mesma
chave RSA, mesmo `sub`/`scope`), só trocando o identificador (CPF em vez de email). É só outro
meio de fazer login — a equipe decidiu manter os dois no app.

## Observabilidade

A aplicação envia **traces**, **métricas** e **logs** para o **Datadog** via OpenTelemetry (OTLP).

**Como funciona:**
- A biblioteca `opentelemetry-spring-boot-starter` instrumenta automaticamente controllers, JPA, JDBC, logs e segurança.
- Traces e métricas são exportados via OTLP/HTTP para `https://otlp.us5.datadoghq.com`.
- Logs são enviados pelo appender `OpenTelemetryAppender` do Logback, com `trace_id` e `span_id` automáticos.
- Datadog correlaciona os três sinais pelo mesmo `trace_id`.

> Se você usar outro site Datadog (EU, AP1, US1, US3, etc.), substitua `us5` pela região correta nos endpoints.

**Variáveis de ambiente obrigatórias em produção:**

```bash
DD_API_KEY=your-datadog-api-key
OTEL_SERVICE_NAME=lata-velha
OTEL_EXPORTER_OTLP_PROTOCOL=http/protobuf
OTEL_EXPORTER_OTLP_ENDPOINT=https://otlp.us5.datadoghq.com
OTEL_EXPORTER_OTLP_TRACES_ENDPOINT=https://otlp.us5.datadoghq.com/v1/traces
OTEL_EXPORTER_OTLP_METRICS_ENDPOINT=https://otlp.us5.datadoghq.com/v1/metrics
OTEL_EXPORTER_OTLP_LOGS_ENDPOINT=https://otlp.us5.datadoghq.com/v1/logs
OTEL_EXPORTER_OTLP_METRICS_TEMPORALITY_PREFERENCE=delta
```

> Em desenvolvimento, sem `DD_API_KEY`, a aplicação continua funcionando normalmente; apenas descarta os dados de telemetria (exporter retorna 403 e segue em frente).

**K8s:** as variáveis de ambiente públicas estão no `infra/k8s/configmap.yaml`; a `DD_API_KEY` fica no `infra/k8s/secret.yaml` e é injetada pelo Terraform via `TF_VAR_dd_api_key`.

## Testes

Rode os testes com:

```bash
mvn clean test
```

Pra ver cobertura (JaCoCo):

```bash
mvn clean verify
```

Gera um relatório em `target/site/jacoco/index.html`. Para abrir:

**Linux/WSL:**

```bash
xdg-open target/site/jacoco/index.html
```

**macOS:**

```bash
open target/site/jacoco/index.html
```

**Windows:**

```bash
start target\site\jacoco\index.html
```

Ou abrir o arquivo no navegador.

**Regra importante:** cobertura tem que ser mínimo 80%. O comando `verify` falha se ficar abaixo disso.

## SonarQube

Para análise estática foi implementado o SonarQube (procura bugs, code smells, duplicação, etc).

Executado com o docker-compose principal:

Abre http://localhost:9000. Login padrão é `admin` / `admin` (vai pedir pra trocar na primeira vez).

Antes de rodar a análise, precisa gerar um token:

1. Avatar (canto direito em cima) → My Account
2. Ir em Security → Generate Tokens
3. Criar um token chamado `lata-velha`
4. Copiar o token

Executar:

```bash
mvn clean test sonar:sonar -Dsonar.token=SEU_TOKEN_AQUI
```

Quando terminar, voltar em http://localhost:9000 e clicar no projeto **Lata-Velha** para ver resultados.

---

## Infraestrutura (Terraform + EKS)

VPC, EKS, RDS, ECR, ALB interno, API Gateway e autoscaling provisionados na AWS com Terraform,
divididos entre quatro repos: [`infra`](https://github.com/lataVelha/lata-velha-pos-tech-fiap-infra)
(base + API Gateway), [`infra-db`](https://github.com/lataVelha/lata-velha-pos-tech-fiap-infra-db)
(banco), [`lambda`](https://github.com/lataVelha/lata-velha-pos-tech-fiap-lambda) (login por CPF +
authorizer) e este repo (deploy da aplicação).

> Documentação do deploy da aplicação: [`terraform/README.md`](terraform/README.md)

---

## Tecnologias

| Tecnologia        | Versão    | Uso                               |
| ----------------- | --------- | --------------------------------- |
| Java              | 21        | Linguagem principal               |
| Spring Boot       | 3.2.5     | Framework web e DI                |
| Spring Security   | via 3.2   | Autenticação JWT com RSA          |
| Spring Data JPA   | via 3.2   | Persistência + Hibernate          |
| PostgreSQL        | 15        | Banco de dados relacional         |
| Flyway            | via 3.2   | Versionamento de migrations       |
| Springdoc OpenAPI | 2.6.0     | Documentação interativa (Swagger) |
| JUnit 5           | via 3.2   | Testes unitários                  |
| Mockito           | via 3.2   | Mocks em testes                   |
| H2 Database       | test      | BD em memória para testes         |
| JaCoCo            | 0.8.12    | Cobertura de testes               |
| SonarQube         | Community | Análise estática de código        |
| Docker Compose    | 3.8       | Orquestração de containers        |
| Terraform         | >= 1.6    | Infraestrutura como código (AWS)  |
| AWS EKS           | 1.36      | Cluster Kubernetes gerenciado     |
| Lombok            | 1.18.32   | Redução de boilerplate            |
| Spring Mail       | via 3.2   | Envio de emails (Gmail SMTP)      |
| Thymeleaf         | via 3.2   | Templates de email                |
| OpenTelemetry     | 2.11.0    | Traces, métricas e logs           |
| Datadog OTLP      | ingest    | Backend de observabilidade        |  