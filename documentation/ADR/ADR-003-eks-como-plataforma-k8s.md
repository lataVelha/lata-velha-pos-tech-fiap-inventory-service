# ADR-003 — AWS EKS como plataforma Kubernetes

## Status

Aceito.

## Contexto

A aplicação principal precisa executar em Kubernetes com capacidade de escalabilidade.

A Fase 3 exige que a aplicação principal execute em um cluster Kubernetes e que a infraestrutura ofereça escalabilidade.  

Como a infraestrutura da solução está provisionada na AWS, foi necessário definir como o cluster Kubernetes seria executado e gerenciado.

O projeto atualmente utiliza AWS EKS como serviço Kubernetes gerenciado. 

## Decisão

Utilizar o Amazon Elastic Kubernetes Service — AWS EKS — para executar a aplicação principal.

O EKS será responsável pelo gerenciamento do cluster Kubernetes enquanto os workloads da aplicação serão executados em pods dentro do cluster.

A arquitetura segue o fluxo:

```text
Internet
   ↓
API Gateway
   ↓
ALB interno
   ↓
AWS EKS
   ↓
Pods da aplicação
```

O cluster também utiliza mecanismos de autoscaling para permitir aumento ou redução de capacidade de acordo com a demanda.

## Alternativas consideradas

### EC2

Executar a aplicação diretamente em máquinas virtuais.

Não foi escolhido porque exigiria maior gerenciamento da infraestrutura e não atenderia diretamente ao requisito de utilização de Kubernetes.

### ECS

Executar containers utilizando o serviço de orquestração próprio da AWS.

Não foi escolhido porque a Fase 3 exige explicitamente uma infraestrutura baseada em Kubernetes.

### Kubernetes autogerenciado

Criar e administrar manualmente um cluster Kubernetes em instâncias EC2.

Não foi escolhido devido ao aumento da complexidade operacional para gerenciamento do control plane, atualizações e disponibilidade do cluster.

### AWS EKS

Utilizar o serviço Kubernetes gerenciado da AWS.

Foi escolhido por atender ao requisito de Kubernetes e reduzir a responsabilidade da equipe sobre o gerenciamento do control plane.

## Consequências

### Positivas

* Kubernetes gerenciado pela AWS;
* integração com outros serviços da AWS;
* suporte a escalabilidade horizontal;
* redução do gerenciamento do control plane;
* integração com ALB, ECR e demais componentes da infraestrutura;
* atende ao requisito de Kubernetes da Fase 3.

### Negativas

* maior complexidade quando comparado à execução direta em uma VM;
* custo adicional para manutenção do cluster;
* exige conhecimento de Kubernetes;
* dependência dos recursos e integrações da AWS.