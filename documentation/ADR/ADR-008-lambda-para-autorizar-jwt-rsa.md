# ADR-008 — JWT RSA com Lambda Authorizer para proteção das APIs

## Status

Aceito.

## Contexto

A Fase 3 exige que rotas sensíveis da aplicação sejam protegidas por autenticação via CPF e que, após a validação do cliente, seja gerado um token JWT válido para consumo das APIs protegidas. 

No projeto, o fluxo de autenticação por CPF gera o mesmo tipo de token JWT utilizado pela aplicação, com assinatura RSA. O acesso às rotas protegidas é validado por uma Lambda Authorizer associada ao API Gateway. 

## Decisão

Utilizar JWT assinado com RSA como mecanismo de autenticação das APIs protegidas e utilizar Lambda Authorizer para validar o token no API Gateway.

O fluxo segue:

```text id="6j5z1h"
Cliente
   ↓
Login por CPF
   ↓
Lambda de autenticação
   ↓
Geração do JWT
   ↓
Cliente envia JWT
   ↓
API Gateway
   ↓
Lambda Authorizer
   ↓
Validação do token
   ↓
Rota protegida
```

O token é assinado com chave privada RSA e validado por meio da chave pública correspondente.

A Lambda Authorizer fica responsável por validar o acesso antes que a requisição seja encaminhada para a aplicação.

## Alternativas consideradas

### Sessão no servidor

Manter o estado de autenticação no backend.

Não foi escolhido porque aumentaria o acoplamento entre as requisições e exigiria gerenciamento de sessão no servidor.

### JWT com chave simétrica

Utilizar uma única chave compartilhada para assinatura e validação dos tokens.

Não foi escolhido porque exigiria compartilhar o mesmo segredo entre os componentes responsáveis por emissão e validação.

### JWT com RSA e Lambda Authorizer

Utilizar chave privada para assinatura do token e chave pública para validação no API Gateway.

Foi escolhido por permitir separar a responsabilidade de emissão e validação do token e proteger as rotas antes que a requisição chegue à aplicação.

## Consequências

### Positivas

* autenticação stateless;
* proteção das rotas no API Gateway;
* separação entre emissão e validação do token;
* chave privada não precisa ser utilizada por todos os componentes;
* integração direta com o fluxo de autenticação via Lambda;
* evita que requisições sem autorização cheguem ao EKS;
* atende diretamente ao requisito de JWT da Fase 3.

### Negativas

* exige gerenciamento seguro das chaves RSA;
* rotação das chaves precisa ser controlada;
* adiciona uma etapa de validação no fluxo das requisições;
* falhas na Lambda Authorizer podem impedir o acesso às APIs protegidas.
