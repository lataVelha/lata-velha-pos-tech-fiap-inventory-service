# RFC-003 — Estratégia de autenticação por CPF

## Status

Aceito.

## Contexto

A Fase 3 exige que as rotas sensíveis da aplicação sejam protegidas por autenticação via CPF.

Também exige uma Function Serverless responsável por:

* validar o CPF;
* consultar a existência e o status do cliente na base de dados;
* gerar e devolver um token JWT válido para consumo das APIs protegidas. 

No projeto, o fluxo de autenticação por CPF foi separado da aplicação principal e implementado no repositório `lambda-aws`. O login por CPF gera o mesmo tipo de token JWT utilizado pela aplicação principal. 

## Objetivo

Definir uma estratégia de autenticação que atenda ao requisito de login por CPF, mantenha a autenticação separada da aplicação principal e permita proteger as APIs expostas pelo API Gateway.

## Alternativas consideradas

### Autenticação dentro da aplicação Spring Boot

Implementar o login por CPF diretamente na aplicação principal.

Pontos avaliados:

* menor quantidade de componentes;
* reutilização da estrutura de segurança existente;
* menor complexidade inicial.

Não foi escolhida porque a Fase 3 exige uma Function Serverless para o fluxo de autenticação.

### Amazon Cognito

Utilizar um serviço gerenciado de identidade da AWS.

Pontos avaliados:

* gerenciamento de usuários;
* emissão de tokens;
* integração com serviços AWS;
* redução da implementação própria de autenticação.

Não foi escolhido porque o projeto precisava manter o fluxo de autenticação baseado em CPF e consultar os dados já existentes na base da aplicação.

### AWS Lambda com JWT

Implementar o fluxo de autenticação em uma Lambda independente.

A Lambda fica responsável por:

* receber CPF e senha;
* validar os dados;
* consultar o cliente;
* verificar sua situação;
* gerar o token JWT.

Foi escolhida por atender diretamente ao requisito da Fase 3 e manter a responsabilidade de autenticação separada da aplicação principal.

## Critérios avaliados

Foram considerados:

* atendimento ao requisito de autenticação por CPF;
* uso obrigatório de Function Serverless;
* integração com API Gateway;
* possibilidade de consulta à base de dados;
* geração de JWT;
* separação de responsabilidades;
* possibilidade de deploy independente;
* integração com a arquitetura AWS já utilizada.

## Decisão

Utilizar AWS Lambda para autenticação por CPF e geração de JWT.

O fluxo fica definido da seguinte forma:

```text
Cliente
   ↓
POST /auth/cpf
   ↓
API Gateway
   ↓
Lambda de autenticação
   ↓
Validação do CPF e senha
   ↓
Consulta do cliente
   ↓
Geração do JWT
   ↓
Token retornado ao cliente
```

Nas requisições seguintes, o token é enviado para acesso às rotas protegidas:

```text
Cliente + JWT
      ↓
API Gateway
      ↓
Lambda Authorizer
      ↓
Aplicação
```

A implementação fica concentrada no repositório:

```text
lambda-aws
```

## Impactos da decisão

### Positivos

* atende diretamente ao requisito de autenticação por CPF;
* atende ao requisito de Function Serverless;
* separa autenticação da aplicação principal;
* permite deploy independente;
* integração direta com API Gateway;
* utiliza JWT para autenticação stateless;
* permite que requisições inválidas sejam bloqueadas antes de chegar à aplicação.

### Negativos

* adiciona novos componentes ao fluxo de autenticação;
* exige comunicação da Lambda com a base de dados;
* exige gerenciamento das chaves utilizadas no JWT;
* falhas na Lambda podem impedir novos logins;
* aumenta a quantidade de recursos de infraestrutura e pipeline que precisam ser mantidos.

## Resultado

Foi definida uma arquitetura de autenticação baseada em:

```text
CPF
 +
AWS Lambda
 +
JWT
 +
Lambda Authorizer
 +
API Gateway
```
