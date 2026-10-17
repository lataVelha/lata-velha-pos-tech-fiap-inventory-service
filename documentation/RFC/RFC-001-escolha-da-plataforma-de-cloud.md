# RFC-001 — Escolha da plataforma de nuvem

## Status

Aceito.

## Contexto

A Fase 3 exige que a solução seja executada em ambiente de nuvem, utilizando:

* API Gateway;
* Function Serverless;
* banco de dados gerenciado;
* cluster Kubernetes com escalabilidade;
* Terraform para provisionamento da infraestrutura. 

Também é necessário manter quatro repositórios separados, com CI/CD e deploy automático para a nuvem. 

Dessa forma, foi necessário definir qual plataforma de nuvem seria utilizada para hospedar e integrar os componentes da solução.

## Objetivo

Escolher uma plataforma de nuvem que permita atender aos requisitos de infraestrutura, escalabilidade, autenticação, banco de dados gerenciado, observabilidade e automação de deploy.

## Alternativas consideradas

### AWS

Serviços considerados:

* Amazon EKS para Kubernetes;
* AWS Lambda para autenticação serverless;
* Amazon API Gateway para exposição das APIs;
* Amazon RDS para PostgreSQL;
* Amazon ECR para imagens Docker;
* Application Load Balancer para comunicação com o EKS;
* integração com Terraform e GitHub Actions.

### Microsoft Azure

Serviços equivalentes:

* Azure Kubernetes Service;
* Azure Functions;
* Azure API Management;
* Azure Database for PostgreSQL;
* Azure Container Registry.

### Google Cloud Platform

Serviços equivalentes:

* Google Kubernetes Engine;
* Cloud Functions;
* API Gateway;
* Cloud SQL for PostgreSQL;
* Artifact Registry.

## Critérios avaliados

Foram considerados os seguintes critérios:

* atendimento aos requisitos obrigatórios da Fase 3;
* disponibilidade de Kubernetes gerenciado;
* suporte a Function Serverless;
* banco de dados gerenciado;
* API Gateway;
* integração com Terraform;
* integração entre os serviços;
* possibilidade de automação via CI/CD;
* facilidade de implementação pela equipe.

## Decisão

Utilizar AWS como plataforma de nuvem da solução.

A arquitetura principal utiliza:

```text
AWS
├── API Gateway
├── Lambda
├── EKS
├── ALB
├── ECR
└── RDS PostgreSQL
```

A AWS foi escolhida por fornecer os serviços necessários para atender à arquitetura definida e permitir integração direta entre os componentes utilizados no projeto.

## Impactos da decisão

### Positivos

* atende aos requisitos obrigatórios da Fase 3;
* possui serviços gerenciados para todos os componentes necessários;
* integração direta entre API Gateway, Lambda, ALB, EKS e RDS;
* suporte nativo a Kubernetes gerenciado;
* suporte a infraestrutura provisionada com Terraform;
* permite deploy automatizado pelos pipelines de CI/CD;
* reduz a necessidade de gerenciamento manual da infraestrutura.

### Negativos

* cria dependência dos serviços da AWS;
* exige conhecimento específico da plataforma;
* custos dependem do consumo e dos recursos provisionados;
* uma futura migração para outra nuvem exigiria adaptação da infraestrutura e das integrações.

## Resultado

A AWS foi definida como plataforma de nuvem da solução.

As decisões específicas sobre os serviços utilizados são detalhadas nos ADRs relacionados a EKS, RDS, API Gateway, Lambda e demais componentes da arquitetura.
