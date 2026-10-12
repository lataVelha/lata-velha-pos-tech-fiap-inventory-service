# ADR-009 — OpenTelemetry e Datadog para observabilidade

## Status

Aceito.

## Contexto

A Fase 3 exige monitoramento e observabilidade da aplicação e da infraestrutura, incluindo:

* latência das APIs;
* consumo de CPU e memória no Kubernetes;
* healthchecks e uptime;
* alertas para falhas no processamento de ordens de serviço;
* logs estruturados em JSON;
* correlação entre requisições;
* dashboards com métricas operacionais e de negócio. 

No projeto, a aplicação já utiliza OpenTelemetry para instrumentação e envia traces, métricas e logs para o Datadog via OTLP. O README também informa que os logs possuem `trace_id` e `span_id`, permitindo correlação entre os sinais de observabilidade. 

## Decisão

Utilizar OpenTelemetry como padrão de instrumentação da aplicação e Datadog como plataforma central de observabilidade.

A arquitetura segue o fluxo:

```text
Aplicação Spring Boot
      ↓
OpenTelemetry
      ↓
OTLP
      ↓
Datadog
```

O OpenTelemetry fica responsável por coletar e exportar:

* traces;
* métricas;
* logs.

O Datadog fica responsável por:

* armazenamento dos dados;
* dashboards;
* análise de métricas;
* consulta de logs;
* visualização de traces;
* criação de alertas.

## Alternativas consideradas

### Logs isolados na aplicação

Manter apenas logs locais ou logs dos containers.

Não foi escolhido porque não atende à necessidade de correlação, monitoramento centralizado e visualização de métricas e traces.

### New Relic

Utilizar New Relic como plataforma de observabilidade.

Não foi escolhido porque o projeto já utiliza Datadog e a solução atende aos requisitos da Fase 3.

### Datadog com OpenTelemetry

Utilizar OpenTelemetry para instrumentação e Datadog como backend de observabilidade.

Foi escolhido por permitir centralizar traces, métricas e logs e manter a instrumentação desacoplada da ferramenta de monitoramento.

## Consequências

### Positivas

* centralização de logs, métricas e traces;
* correlação de requisições por `trace_id` e `span_id`;
* monitoramento de comportamento da aplicação;
* suporte à criação de dashboards;
* suporte à criação de alertas;
* possibilidade de acompanhar latência e falhas das APIs;
* OpenTelemetry reduz o acoplamento direto com o Datadog;
* atende diretamente aos requisitos de observabilidade da Fase 3.

### Negativas

* exige configuração correta dos exporters OTLP;
* depende da disponibilidade do Datadog;
* pode gerar custo conforme o volume de dados coletados;
* excesso de logs, métricas ou traces pode aumentar o custo e dificultar a análise.
