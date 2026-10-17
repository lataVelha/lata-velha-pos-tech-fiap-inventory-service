# ADR-005 — API Gateway como ponto de entrada público

## Status

Aceito.

## Contexto

A aplicação precisa expor APIs para consumo externo, incluindo rotas públicas e rotas protegidas por autenticação.

A Fase 3 exige o uso de um API Gateway para controle e roteamento das requisições e também exige que rotas sensíveis sejam protegidas por autenticação via CPF. 

Na arquitetura atual, o ALB não é exposto diretamente para a internet. Todo tráfego externo passa primeiro pelo API Gateway. 

## Decisão

Utilizar AWS API Gateway como ponto de entrada público da aplicação.

O API Gateway será responsável por receber as requisições externas, aplicar as regras de autenticação e encaminhar o tráfego para os serviços internos da aplicação.

A arquitetura segue o fluxo:

```text
Cliente
   ↓
AWS API Gateway
   ↓
ALB interno
   ↓
AWS EKS
   ↓
Aplicação
```

As rotas protegidas utilizam autenticação baseada em JWT e Lambda Authorizer antes do encaminhamento para a aplicação.

## Alternativas consideradas

### Expor o ALB diretamente

Permitir acesso público direto ao ALB da aplicação.

Não foi escolhido porque deixaria a camada de entrada da aplicação diretamente exposta e reduziria a centralização das regras de autenticação e roteamento.

### Kong

Utilizar Kong como API Gateway.

Não foi escolhido porque exigiria implantação e manutenção adicional dentro da infraestrutura.

### Traefik

Utilizar Traefik como gateway e ingress controller.

Não foi escolhido porque a solução já utiliza serviços gerenciados da AWS e o API Gateway oferece integração direta com os demais componentes utilizados.

### AWS API Gateway

Utilizar o serviço gerenciado da AWS como entrada pública da solução.

Foi escolhido por permitir centralizar roteamento, autenticação e exposição das APIs sem disponibilizar diretamente o ALB ou o cluster Kubernetes.

## Consequências

### Positivas

* ponto único de entrada para as APIs;
* ALB e EKS permanecem privados;
* centralização das regras de autenticação;
* integração com Lambda Authorizer;
* integração com os demais serviços da AWS;
* separação entre exposição pública e infraestrutura interna;
* atende diretamente ao requisito de API Gateway da Fase 3.

### Negativas

* adiciona uma camada extra no fluxo das requisições;
* pode aumentar a latência das chamadas;
* exige configuração e manutenção das rotas no API Gateway;
* cria dependência adicional de um serviço gerenciado da AWS.
