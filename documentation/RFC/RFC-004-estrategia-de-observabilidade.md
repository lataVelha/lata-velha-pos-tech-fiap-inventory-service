# RFC-004 — Estratégia de observabilidade

## Status

Aceito.

## Contexto

A Fase 3 exige monitoramento e observabilidade da aplicação e da infraestrutura.

Entre os itens obrigatórios estão:

* latência das APIs;
* consumo de CPU e memória no Kubernetes;
* healthchecks e uptime;
* alertas para falhas no processamento de ordens de serviço;
* logs estruturados em JSON;
* correlação entre requisições;
* dashboards com métricas operacionais e de negócio. 

No projeto, a aplicação utiliza OpenTelemetry para instrumentação e envio de traces, métricas e logs. O Datadog é utilizado como backend de observabilidade via OTLP. 

## Objetivo

Definir uma estratégia de observabilidade que permita acompanhar o comportamento da aplicação, identificar falhas, analisar desempenho e centralizar logs, métricas e traces.

## Alternativas consideradas

### Logs locais da aplicação

Manter apenas logs gerados pelos containers e pela aplicação.

Pontos avaliados:

* implementação simples;
* baixo custo inicial;
* fácil acesso durante desenvolvimento.

Não foi escolhido porque não atende à necessidade de centralização, correlação entre requisições, dashboards, métricas e traces.

### New Relic

Utilizar New Relic como plataforma central de observabilidade.

Pontos avaliados:

* APM;
* logs;
* métricas;
* tracing;
* dashboards;
* alertas.

Não foi escolhido porque a solução já estava integrada ao Datadog e não havia necessidade de adicionar uma segunda plataforma.

### Datadog com OpenTelemetry

Utilizar OpenTelemetry como padrão de instrumentação e Datadog como plataforma de armazenamento, análise e visualização.

Foi escolhido por permitir centralizar os principais sinais de observabilidade e manter a instrumentação da aplicação desacoplada do fornecedor.

## Critérios avaliados

Foram considerados:

* suporte a logs, métricas e traces;
* correlação entre requisições;
* monitoramento de APIs;
* monitoramento de Kubernetes;
* criação de dashboards;
* criação de alertas;
* integração com Spring Boot;
* integração com OpenTelemetry;
* atendimento aos requisitos da Fase 3.

## Decisão

Utilizar OpenTelemetry para instrumentação e Datadog como plataforma central de observabilidade.

O fluxo fica definido da seguinte forma:

```text
Aplicação Spring Boot
      ↓
OpenTelemetry
      ↓
OTLP
      ↓
Datadog
```

A aplicação envia:

* traces;
* métricas;
* logs.

O Datadog fica responsável por:

* dashboards;
* análise de latência;
* acompanhamento de erros;
* visualização de logs;
* consulta de traces;
* alertas;
* monitoramento da aplicação e do Kubernetes.

## Impactos da decisão

### Positivos

* centraliza logs, métricas e traces;
* permite correlação por `trace_id` e `span_id`;
* facilita análise de falhas;
* permite acompanhar latência das APIs;
* suporta dashboards e alertas;
* OpenTelemetry reduz o acoplamento direto com o Datadog;
* atende aos requisitos de observabilidade da Fase 3.

### Negativos

* depende da disponibilidade do Datadog;
* pode gerar custo conforme o volume de telemetria;
* exige configuração correta dos exporters OTLP;
* excesso de dados pode dificultar análise e aumentar custo;
* dashboards e alertas precisam ser mantidos conforme o sistema evolui.

## Resultado

Foi definida a seguinte estratégia de observabilidade:

```text
OpenTelemetry
      +
Datadog
      +
Logs
      +
Métricas
      +
Traces
```
