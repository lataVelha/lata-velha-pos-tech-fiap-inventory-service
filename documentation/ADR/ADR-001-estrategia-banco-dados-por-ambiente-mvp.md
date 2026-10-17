# ADR-001 — Estratégia de Banco de Dados por Ambiente no MVP

## Status

Aceito.

---

## Contexto

O MVP do Sistema de Gestão de Ordem de Serviço da oficina mecânica tem como objetivo atender os principais fluxos operacionais do domínio, envolvendo cadastro de proprietários, veículos, serviços, peças, abertura da Ordem de Serviço, diagnóstico, aprovação, execução do serviço e controle de estoque. Nesse cenário, a aplicação será desenvolvida com Java 21 e Spring Boot, seguindo uma arquitetura em camadas orientada por Domain-Driven Design.

Por se tratar de um MVP, neste primeiro momento a aplicação será executada apenas localmente na máquina dos desenvolvedores ou por meio de Docker. Ainda não há um ambiente de produção provisionado, porém a escolha do banco de dados deve considerar a possibilidade de evolução futura do sistema para um ambiente produtivo ou infraestrutura em cloud.

Dado o contexto do domínio, grande parte das informações manipuladas possui relacionamento direto entre si. Uma Ordem de Serviço está associada a um Proprietário e a um Veículo, pode possuir Serviços identificados durante o Diagnóstico, depende da aprovação do Proprietário e pode demandar Peças Alocadas para sua Execução. Além disso, o controle de Estoque precisa manter consistência nas operações de reserva e baixa de Peças.

Desse modo, torna-se necessário escolher uma solução de persistência adequada para dados relacionais, com suporte a transações e integridade entre as principais entidades do sistema. Também é importante considerar que o time de desenvolvimento possui maior experiência com PostgreSQL, o que reduz a curva de aprendizado e os riscos técnicos durante a construção do MVP.

---

## Decisão

Decidimos utilizar o PostgreSQL como banco de dados principal do MVP para os ambientes de desenvolvimento local e execução via Docker.

A escolha foi feita porque o PostgreSQL é um banco de dados relacional maduro, adequado para sistemas com entidades fortemente relacionadas e regras de negócio que dependem de consistência transacional. No contexto da oficina, essa característica é importante para manter a integridade entre Ordem de Serviço, Proprietário, Veículo, Serviço, Peça, Estoque e Execução do Serviço. Além disso, a experiência prévia do time com PostgreSQL favorece a implementação, configuração e resolução de problemas no decorrer do projeto.

Também decidimos utilizar o H2 apenas para os testes unitários. Essa escolha foi feita porque o H2 é um banco em memória, leve e de fácil configuração, permitindo que os testes sejam executados de forma rápida e isolada, sem a necessidade de subir um banco externo durante a validação das regras de negócio.

Além disso, decidimos utilizar migrations, preferencialmente com Flyway, para controlar a evolução da estrutura do banco de dados desde o início do MVP.

Vale ressaltar que esta decisão não define ainda um ambiente de produção. Caso o sistema evolua para produção, a estratégia deverá ser reavaliada ou complementada por uma nova ADR, mantendo o PostgreSQL como opção preferencial caso as premissas atuais continuem válidas.

---

## Consequências

A adoção do PostgreSQL favorece a modelagem relacional do sistema, permitindo representar de forma clara os vínculos entre as entidades do domínio. Essa escolha contribui para reduzir inconsistências, como uma Ordem de Serviço sem vínculo correto com um Veículo, uma Peça Alocada sem relação com uma Execução do Serviço ou uma baixa de estoque feita sem respeitar o estado atual da OS.

Outro ponto positivo é que o uso do PostgreSQL desde o desenvolvimento local evita que o sistema seja construído sobre um banco simplificado demais para as necessidades reais do domínio. Mesmo que o MVP ainda rode apenas localmente ou via Docker, o projeto já passa a utilizar uma tecnologia próxima de um possível ambiente produtivo.

O uso de migrations permite controlar de forma explícita as mudanças na estrutura do banco, tornando a criação de tabelas, relacionamentos, constraints e índices mais versionada e reproduzível.

Quanto aos testes unitários, o uso do H2 simplifica a execução da suíte de testes, pois não depende de instalação local, container ou conexão com um banco externo. Isso permite um ciclo de feedback mais rápido durante o desenvolvimento.

Por outro lado, é importante considerar que o H2 não reproduz integralmente o comportamento do PostgreSQL. Podem existir diferenças de dialeto SQL, tipos de dados, constraints e comportamento transacional. Por esse motivo, o H2 deve ser utilizado apenas nos testes unitários.

Caso futuramente sejam criados testes de integração mais próximos do ambiente real, principalmente envolvendo migrations, queries específicas, concorrência, reserva de estoque e baixa de peças, recomenda-se avaliar o uso do PostgreSQL via Docker ou Testcontainers.

---

## Alternativas consideradas

### PostgreSQL

**Resultado:** aceito para desenvolvimento local e execução via Docker.

O PostgreSQL foi escolhido por sua aderência ao domínio relacional e transacional da oficina. Como o sistema precisa manter consistência entre Ordens de Serviço, Proprietários, Veículos, Serviços, Peças, Estoque e Execuções do Serviço, um banco relacional se mostra mais adequado para representar esses vínculos e proteger as operações críticas do fluxo.

Além disso, o PostgreSQL possui boa integração com o ecossistema Java e Spring Boot, sendo compatível com ferramentas de persistência, migrations e execução via Docker. A experiência prévia do time com essa tecnologia também contribuiu para a decisão, por reduzir riscos e acelerar a construção do MVP.

---

### H2

**Resultado:** aceito para testes unitários.

O H2 foi escolhido por ser uma solução simples e leve para execução de testes unitários. Como ele pode rodar em memória, os testes não dependem de um banco externo, reduzindo o tempo de execução e facilitando a validação automatizada durante o desenvolvimento.

No entanto, seu uso fica restrito aos testes unitários, pois ele não possui o mesmo comportamento do PostgreSQL em todos os cenários.

---

### MySQL

**Resultado:** recusado.

O MySQL também foi considerado por ser uma opção relacional madura e amplamente utilizada no mercado. Ele poderia atender parte das necessidades do sistema, principalmente por também permitir modelagem relacional e uso de transações.

Contudo, o PostgreSQL foi considerado mais adequado para este MVP por sua robustez em cenários transacionais, boa aderência a modelos relacionais mais elaborados, integração com o ecossistema técnico previsto e maior familiaridade do time.

---

### MongoDB

**Resultado:** recusado.

O MongoDB foi considerado por ser uma opção NoSQL orientada a documentos, útil em cenários onde os dados possuem estrutura mais flexível e menos dependência de relacionamentos rígidos.

No entanto, o domínio do sistema da oficina não é predominantemente documental. A Ordem de Serviço depende de relações consistentes com Proprietário, Veículo, Serviços, Peças, Estoque e Execução do Serviço. Desse modo, a adoção de um banco documental poderia aumentar a complexidade da modelagem e das garantias de consistência, sem trazer uma vantagem clara para o escopo atual do MVP.
