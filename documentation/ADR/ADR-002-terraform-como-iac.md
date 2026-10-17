# ADR-002 — Terraform como padrão de Infrastructure as Code

## Status

Aceito.

## Contexto

A solução precisa provisionar recursos de infraestrutura na AWS para aplicação, banco de dados, Kubernetes, rede e componentes de integração.

A Fase 3 exige o uso de Infrastructure as Code e define Terraform como requisito para o provisionamento da infraestrutura. 

Atualmente o Terraform é utilizado nos repositórios responsáveis pela infraestrutura e também no deploy dos recursos Kubernetes da aplicação. 

## Decisão

Utilizar Terraform como ferramenta padrão de Infrastructure as Code da solução.

O Terraform será responsável pelo provisionamento e manutenção dos recursos de infraestrutura utilizados pelo sistema.

Cada repositório mantém somente os recursos relacionados à sua responsabilidade.

Exemplo:

```text
infra
→ VPC
→ EKS
→ ECR
→ ALB
→ API Gateway
→ Cluster Autoscaler

infra-db
→ RDS PostgreSQL

app
→ Recursos Kubernetes da aplicação
→ Integração da aplicação com ALB e API Gateway
```

## Alternativas consideradas

### Provisionamento manual pela AWS Console

Criar e alterar os recursos diretamente pela interface da AWS.

Não foi escolhido porque dificulta reproduzir o ambiente, controlar alterações e manter histórico da infraestrutura.

### AWS CloudFormation

Utilizar a solução nativa da AWS para Infrastructure as Code.

Não foi escolhida porque o projeto já utiliza Terraform e a equipe optou por manter uma única ferramenta de provisionamento.

### Terraform

Definir a infraestrutura de forma declarativa e versionada junto aos repositórios.

Foi escolhido por permitir automatização, versionamento e integração direta com os pipelines de CI/CD.

## Consequências

### Positivas

* infraestrutura versionada;
* ambientes reproduzíveis;
* provisionamento automatizado;
* integração com GitHub Actions;
* alterações de infraestrutura podem passar por Pull Request;
* separação dos states por responsabilidade.

### Negativas

* exige gerenciamento dos states;
* dependências entre os repositórios precisam ser controladas;
* mudanças incorretas no Terraform podem impactar recursos de infraestrutura.