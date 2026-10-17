# ADR-004 — AWS RDS PostgreSQL como banco de dados gerenciado

## Status

Aceito.

## Contexto

A aplicação precisa persistir dados de clientes, veículos, ordens de serviço, serviços, peças, estoque e autenticação.

A Fase 3 exige o uso de um banco de dados gerenciado e também pede uma justificativa formal da escolha do banco utilizado.  

O projeto utiliza PostgreSQL 15 como banco relacional da aplicação e AWS RDS como serviço gerenciado para execução do banco em ambiente de nuvem. 

## Decisão

Utilizar PostgreSQL como banco de dados relacional e AWS RDS como serviço gerenciado.

O banco será responsável pela persistência dos dados da aplicação, enquanto o RDS ficará responsável pela infraestrutura de execução e gerenciamento do banco na AWS.

A arquitetura segue o fluxo:

```text
Aplicação no EKS
      ↓
PostgreSQL
      ↓
AWS RDS
```

O provisionamento do banco fica isolado no repositório:

```text
infra-db
```

Esse repositório possui seu próprio Terraform, state e pipeline de CI/CD. 

## Alternativas consideradas

### MySQL

Banco relacional amplamente utilizado e disponível como serviço gerenciado na AWS.

Não foi escolhido porque o projeto já utiliza PostgreSQL e sua estrutura relacional atende às necessidades do domínio da aplicação.

### Banco NoSQL

Utilizar uma solução como DynamoDB.

Não foi escolhido porque o domínio possui diversos relacionamentos entre entidades, como proprietários, veículos, ordens de serviço, serviços, peças e estoque.

A modelagem relacional atende melhor à necessidade de integridade e relacionamento entre esses dados.

### PostgreSQL executado dentro do Kubernetes

Executar o banco como workload dentro do próprio cluster EKS.

Não foi escolhido porque aumentaria a responsabilidade operacional da equipe sobre persistência, backup, disponibilidade e manutenção do banco.

### PostgreSQL no AWS RDS

Utilizar PostgreSQL em um serviço gerenciado da AWS.

Foi escolhido por manter o modelo relacional já utilizado pela aplicação e reduzir a responsabilidade da equipe sobre a infraestrutura do banco.

## Consequências

### Positivas

* banco de dados gerenciado pela AWS;
* redução da responsabilidade operacional sobre o banco;
* manutenção do PostgreSQL já utilizado pela aplicação;
* suporte a relacionamentos e integridade referencial;
* integração com Spring Data JPA e Hibernate;
* infraestrutura do banco isolada no repositório `infra-db`;
* atende diretamente ao requisito de banco gerenciado da Fase 3.

### Negativas

* dependência do serviço RDS da AWS;
* custo adicional em relação a um banco executado localmente ou dentro do cluster;
* exige controle de rede e acesso entre EKS e RDS;
* alterações estruturais no banco precisam ser coordenadas com a aplicação.
