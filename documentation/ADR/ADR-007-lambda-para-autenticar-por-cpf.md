# ADR-007 — Lambda Serverless para autenticação por CPF

## Status

Aceito.

## Contexto

A Fase 3 exige que rotas sensíveis sejam protegidas por autenticação via CPF e que exista uma Function Serverless responsável por validar o CPF, consultar a existência e o status do cliente na base de dados e gerar um token JWT para acesso às APIs protegidas. 

No projeto, essa responsabilidade foi separada da aplicação principal e implementada no repositório `lambda-aws`. O README também registra que existe um fluxo de login por CPF fora do repositório principal da aplicação. 

## Decisão

Utilizar AWS Lambda como Function Serverless responsável pelo fluxo de autenticação por CPF.

A Lambda recebe os dados de autenticação, valida o CPF, consulta o cliente e, quando a autenticação é válida, gera um token JWT para consumo das rotas protegidas.

O fluxo segue:

```text
Cliente
   ↓
API Gateway
   ↓
Lambda de autenticação
   ↓
Consulta cliente
   ↓
Geração do JWT
   ↓
Token retornado ao cliente
```

A implementação fica isolada no repositório:

```text
lambda-aws
```

Esse repositório possui seu próprio código, infraestrutura e pipeline de CI/CD.

## Alternativas consideradas

### Autenticação dentro da aplicação principal

Manter o login por CPF dentro da aplicação Spring Boot.

Não foi escolhido porque a Fase 3 exige uma Function Serverless para autenticação.

### Amazon Cognito

Utilizar o serviço gerenciado de identidade da AWS.

Não foi escolhido porque o projeto precisava manter o fluxo próprio de autenticação por CPF e integração com os dados existentes da aplicação.

### AWS Lambda

Implementar a autenticação em uma função serverless independente.

Foi escolhido por atender diretamente ao requisito da Fase 3 e permitir separar a responsabilidade de autenticação da aplicação principal.

## Consequências

### Positivas

* atende diretamente ao requisito de Function Serverless;
* separa autenticação da aplicação principal;
* permite deploy independente;
* reduz acoplamento entre autenticação e aplicação;
* integração direta com API Gateway;
* pode escalar de forma independente conforme a demanda;
* mantém a responsabilidade concentrada no repositório `lambda-aws`.

### Negativas

* adiciona um novo componente na arquitetura;
* exige comunicação com a base de dados;
* aumenta a quantidade de pipelines e recursos que precisam ser mantidos;
* falhas na Lambda podem impedir o acesso às APIs protegidas.
