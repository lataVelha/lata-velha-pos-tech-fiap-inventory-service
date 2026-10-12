# RFC-002 — Escolha do banco de dados gerenciado

## Status

Aceito.

## Contexto

A Fase 3 exige o uso de um banco de dados gerenciado e também solicita uma justificativa formal para a escolha do banco utilizado, além da documentação do modelo relacional e seus relacionamentos.  

A aplicação trabalha com dados fortemente relacionados, como:

* proprietários;
* veículos;
* ordens de serviço;
* serviços;
* peças;
* estoque;
* autenticação.

O projeto já utiliza PostgreSQL 15 como banco relacional principal. 

## Objetivo

Escolher um banco de dados gerenciado que atenda às necessidades de consistência, relacionamento entre entidades, integração com a aplicação e operação em ambiente de nuvem.

## Alternativas consideradas

### PostgreSQL no AWS RDS

Utilizar PostgreSQL como banco relacional e AWS RDS como serviço gerenciado.

Pontos avaliados:

* suporte a relacionamentos complexos;
* integridade referencial;
* transações;
* integração com Spring Data JPA e Hibernate;
* compatibilidade com a modelagem atual;
* serviço gerenciado pela AWS.

### MySQL no AWS RDS

Utilizar MySQL também por meio do AWS RDS.

Pontos avaliados:

* banco relacional amplamente utilizado;
* suporte a transações;
* integração com aplicações Java;
* disponibilidade como serviço gerenciado.

Não foi escolhido porque o projeto já está estruturado sobre PostgreSQL e não havia necessidade técnica de alterar a tecnologia adotada.

### SQL Server

Utilizar Microsoft SQL Server como banco relacional gerenciado.

Pontos avaliados:

* suporte a transações;
* modelo relacional;
* recursos avançados de banco.

Não foi escolhido por adicionar uma tecnologia diferente da já utilizada no projeto e sem benefício necessário para os requisitos atuais.

### Banco NoSQL

Utilizar uma solução como DynamoDB.

Não foi escolhido porque o domínio possui vários relacionamentos entre entidades e depende de consistência entre dados como OS, veículo, proprietário, serviços, peças e estoque.

Para esse cenário, o modelo relacional utilizado atualmente é mais adequado.

## Critérios avaliados

Foram considerados:

* aderência ao modelo relacional do sistema;
* suporte a transações;
* integridade referencial;
* facilidade de integração com Spring Boot;
* compatibilidade com JPA e Hibernate;
* suporte no ambiente AWS;
* facilidade de operação;
* impacto de migração;
* atendimento aos requisitos da Fase 3.

## Decisão

Utilizar PostgreSQL 15 executando no AWS RDS.

A arquitetura fica definida da seguinte forma:

```text
Aplicação no EKS
      ↓
Spring Data JPA
      ↓
PostgreSQL
      ↓
AWS RDS
```

A infraestrutura do banco fica isolada no repositório:

```text
infra-db
```

Esse repositório é responsável pelo provisionamento do banco por Terraform e possui pipeline de CI/CD próprio. 

## Impactos da decisão

### Positivos

* mantém compatibilidade com a aplicação atual;
* atende bem ao modelo relacional do domínio;
* suporte a transações e integridade referencial;
* integração direta com Spring Data JPA e Hibernate;
* reduz a responsabilidade operacional da equipe sobre a infraestrutura do banco;
* permite provisionamento automatizado com Terraform;
* atende ao requisito de banco gerenciado da Fase 3.

### Negativos

* dependência do AWS RDS;
* custo associado ao serviço gerenciado;
* exige configuração de rede entre EKS e RDS;
* alterações estruturais precisam ser controladas por migrations;
* mudanças futuras para outro banco podem exigir adaptações na aplicação.

## Resultado

PostgreSQL 15 no AWS RDS foi definido como banco de dados gerenciado da solução.
