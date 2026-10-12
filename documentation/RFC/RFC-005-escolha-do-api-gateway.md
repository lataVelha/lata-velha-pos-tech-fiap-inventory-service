# RFC-005 — Escolha do API Gateway

## Status

Aceito.

## Contexto

A Fase 3 exige o uso de um API Gateway para controle e roteamento das requisições da aplicação. Também exige que as rotas sensíveis sejam protegidas por autenticação via CPF. 

Na arquitetura atual, todo tráfego externo passa pelo API Gateway. O ALB permanece interno e não é acessível diretamente pela internet. 

## Objetivo

Definir uma estratégia de entrada única para as APIs, permitindo:

* controle do tráfego externo;
* roteamento das requisições;
* proteção das rotas sensíveis;
* integração com autenticação;
* redução da exposição direta da infraestrutura interna.

## Alternativas consideradas

### ALB público

Expor diretamente o Application Load Balancer para a internet.

Pontos avaliados:

* arquitetura mais simples;
* menor quantidade de componentes;
* encaminhamento direto para o EKS.

Não foi escolhido porque reduziria o controle centralizado das rotas e deixaria o ALB diretamente exposto.

### Kong

Utilizar Kong como API Gateway.

Pontos avaliados:

* controle de rotas;
* plugins;
* autenticação;
* possibilidade de execução dentro do Kubernetes.

Não foi escolhido porque exigiria manutenção adicional da própria infraestrutura do gateway.

### Traefik

Utilizar Traefik como gateway e proxy reverso.

Pontos avaliados:

* integração com Kubernetes;
* roteamento;
* configuração relativamente simples.

Não foi escolhido porque a solução já utiliza serviços gerenciados da AWS e o projeto precisava manter a entrada pública fora do cluster.

### AWS API Gateway

Utilizar o serviço gerenciado da AWS como ponto de entrada público.

Foi escolhido por permitir integração direta com Lambda, Lambda Authorizer e demais recursos da arquitetura AWS.

## Critérios avaliados

Foram considerados:

* atendimento ao requisito da Fase 3;
* integração com autenticação por CPF;
* integração com Lambda Authorizer;
* controle centralizado das rotas;
* redução da exposição do EKS;
* integração com os demais serviços AWS;
* manutenção da infraestrutura;
* possibilidade de separar camada pública e camada privada.

## Decisão

Utilizar AWS API Gateway como ponto de entrada público da solução.

O fluxo fica definido da seguinte forma:

```text
Cliente
   ↓
AWS API Gateway
   ↓
Lambda Authorizer
   ↓
ALB interno
   ↓
AWS EKS
   ↓
Aplicação
```

Para o fluxo de autenticação por CPF:

```text
Cliente
   ↓
API Gateway
   ↓
Lambda de autenticação
   ↓
JWT
```

Dessa forma, o API Gateway centraliza a exposição das APIs enquanto o ALB e o EKS permanecem na camada interna da arquitetura.

## Impactos da decisão

### Positivos

* atende diretamente ao requisito de API Gateway;
* centraliza o acesso externo;
* integra diretamente com Lambda e Lambda Authorizer;
* mantém o ALB privado;
* reduz a exposição direta do EKS;
* separa claramente camada pública e camada interna;
* facilita aplicação de regras de autenticação e roteamento.

### Negativos

* adiciona uma camada extra no fluxo das requisições;
* pode aumentar a latência;
* exige configuração e manutenção das rotas;
* cria dependência adicional de um serviço gerenciado da AWS;
* falhas no API Gateway podem impedir o acesso às APIs.

## Resultado

Foi definida a seguinte estratégia de entrada e roteamento:

```text
AWS API Gateway
      +
Lambda Authorizer
      +
ALB interno
      +
AWS EKS
```
