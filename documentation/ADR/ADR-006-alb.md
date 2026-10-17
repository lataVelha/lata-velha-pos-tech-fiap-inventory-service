# ADR-006 — ALB interno como comunicação entre API Gateway e EKS

## Status

Aceito.

## Contexto

A aplicação principal executa dentro do cluster EKS e não deve ficar exposta diretamente para a internet.

Na arquitetura atual, o ALB é privado e funciona como ponto de entrada interno para os workloads executados no Kubernetes. Todo tráfego externo passa primeiro pelo API Gateway. 

A Fase 3 exige API Gateway para controle e roteamento, além de cluster Kubernetes com escalabilidade. 

## Decisão

Utilizar um Application Load Balancer interno entre o API Gateway e o cluster EKS.

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

O ALB não possui acesso público direto e fica responsável por encaminhar as requisições recebidas do API Gateway para os serviços da aplicação dentro do EKS.

## Alternativas consideradas

### ALB público

Expor o ALB diretamente para a internet.

Não foi escolhido porque permitiria acesso direto à camada interna da aplicação e reduziria o controle centralizado oferecido pelo API Gateway.

### Acesso direto ao EKS

Permitir acesso externo diretamente aos serviços do cluster.

Não foi escolhido porque aumentaria a exposição da infraestrutura Kubernetes e dificultaria o controle de entrada das requisições.

### ALB interno

Manter o balanceador acessível apenas dentro da infraestrutura privada.

Foi escolhido para separar a camada pública da camada interna e manter o cluster protegido atrás do API Gateway.

## Consequências

### Positivas

* EKS não fica exposto diretamente para a internet;
* ALB permanece em rede privada;
* separação clara entre entrada pública e infraestrutura interna;
* centralização do acesso externo pelo API Gateway;
* integração nativa com serviços executados no EKS;
* distribuição de tráfego entre os workloads da aplicação;
* melhora o isolamento da arquitetura.

### Negativas

* adiciona uma camada adicional no fluxo das requisições;
* aumenta a quantidade de recursos de infraestrutura;
* exige configuração de rede entre API Gateway, ALB e EKS;
* falhas no ALB podem impactar o acesso à aplicação.
