# Infraestrutura — Lata Velha (AWS + EKS)

Infraestrutura como código para o projeto **Lata Velha** na AWS usando **Terraform >= 1.10** (lock de estado nativo do S3).

Compatível com **AWS Academy (Learner Lab)** — usa a `LabRole` pré-existente sem precisar criar IAM roles ou OIDC providers.

---

## Sumário

- [Como executar](#como-executar)
- **Arquitetura**
  - [Visão geral](#visão-geral)
  - [O que é provisionado](#o-que-é-provisionado)
  - [Segurança & disponibilidade](#segurança--disponibilidade)
  - [Estrutura de arquivos](#estrutura-de-arquivos)
- **Explicações**
  - [Compatibilidade com AWS Academy](#compatibilidade-com-aws-academy)
  - [Variáveis do Terraform](#variáveis-do-terraform)
  - [Custo aproximado (us-east-1)](#custo-aproximado-us-east-1)
- **CI/CD (GitHub Actions)** — caminho principal
  - [Pipeline e configuração de secrets](#pipeline-e-configuração-de-secrets)
  - [Jobs do pipeline](#jobs-do-pipeline)
- **Execução local** — opcional
  - [Pré-requisitos locais](#pré-requisitos-locais)
  - [Comandos Terraform diretos (sem apply.sh)](#comandos-terraform-diretos-sem-applysh)
  - [Verificando o deploy](#verificando-o-deploy)
  - [Flags do apply.sh](#flags-do-applysh)
  - [O que o apply.sh faz por dentro](#o-que-o-applysh-faz-por-dentro)

---

## Como executar

- **Recomendado (CI):** push para `master` → pipeline roda automaticamente
- **Local (opcional, mesmo fluxo):** `./apply.sh --auto` — útil para iterar sem esperar o runner
- **Manual / depuração:** seção "Comandos Terraform diretos" abaixo

---

## Arquitetura

### Visão geral

![Diagrama da arquitetura AWS](../documentation/arquitetura-aws.svg)

![Pipeline CI/CD](../documentation/pipeline-cicd.svg)

### Por que ALB gerenciado pelo Terraform e não pelo AWS Load Balancer Controller?

O **ALB Controller** cria recursos AWS (ALBs, Security Groups, ENIs) fora do estado
do Terraform. No `destroy`, esses recursos bloqueiam a deleção da VPC e exigem scripts
de limpeza externos — uma solução frágil.

Com o ALB provisionado diretamente pelo Terraform via `aws_lb`, `aws_lb_target_group`
e `aws_autoscaling_attachment`, o `terraform destroy` remove tudo na ordem certa,
sem scripts auxiliares.

O tráfego chega ao ALB na porta 80 e é encaminhado para o **NodePort 30080** de
qualquer node do EKS. A associação ao ASG (`aws_autoscaling_attachment`) registra
e desregistra nodes automaticamente conforme o cluster escala.

---

### O que é provisionado

#### Módulo `bootstrap` — infraestrutura base

| Recurso        | Descrição                                                             |
| -------------- | --------------------------------------------------------------------- |
| VPC            | CIDR `10.0.0.0/16`, 2 AZs, subnets públicas + privadas + NAT Gateway |
| ECR            | Repositório Docker privado (`lata-velha`)                             |
| EKS Cluster    | Control plane gerenciado, Kubernetes 1.36                             |
| EKS Node Group | EC2 `t3.small`, autoscaling: desejado 2 / min 1 / max 4              |
| RDS PostgreSQL | `db.t3.micro`, 20 GB, **criptografado**, subnet privada, sem multi-AZ |

#### Módulo `deploy` — aplicação e roteamento

| Recurso AWS              | Descrição                                                         |
| ------------------------ | ----------------------------------------------------------------- |
| ALB                      | Application Load Balancer internet-facing, HTTP 80                |
| Target Group             | `target_type=instance`, NodePort 30080, health check `/actuator/health/readiness` |
| Listener                 | HTTP 80 → forward para o Target Group                             |
| Security Group (ALB)     | Permite entrada HTTP 80 da internet                               |
| Security Group Rule      | Permite tráfego ALB → nodes EKS na porta 30080                    |
| ASG Attachment           | Registra automaticamente os nodes EKS no Target Group             |

| Recurso Kubernetes       | Descrição                                                         |
| ------------------------ | ----------------------------------------------------------------- |
| Namespace                | `lata-velha`                                                      |
| Secret `aws-credentials` | Credenciais AWS para o Cluster Autoscaler (kube-system)           |
| ConfigMap                | Variáveis não-sensíveis da aplicação (inclui endpoints OTLP/US5) |
| Secret                   | Credenciais do banco, e-mail e Datadog API key (base64 via TF)   |
| Deployment               | 2 réplicas, probes liveness/readiness/startup, `readOnlyRootFilesystem`, anti-affinity |
| Service                  | NodePort 30080 → 8080                                             |
| HPA                      | Autoscaling por CPU (60%) entre 2 e 6 réplicas                    |
| PDB                      | PodDisruptionBudget — no máx. 1 pod indisponível em manutenção    |
| Cluster Autoscaler       | Adiciona/remove nodes EC2 conforme demanda                        |
| Metrics Server           | Coleta CPU/memória dos pods — obrigatório para o HPA              |

---

### Segurança & disponibilidade

Boas práticas aplicadas além do provisionamento básico:

- **Estado do Terraform com lock nativo do S3** (`use_lockfile`) — impede dois `apply` simultâneos de corromperem o state, sem precisar de DynamoDB.
- **RDS criptografado em repouso** (`storage_encrypted`, chave gerenciada `aws/rds`).
- **Banco isolado** — Security Group libera a porta 5432 apenas de dentro da VPC; sem acesso público.
- **Container endurecido** — `runAsNonRoot`, `readOnlyRootFilesystem` (com `/tmp` em `emptyDir`) e `allowPrivilegeEscalation: false`.
- **Alta disponibilidade** — réplicas espalhadas entre nodes (`podAntiAffinity`) e um **PodDisruptionBudget** que mantém ao menos 1 pod servindo durante manutenções.
- **Autoscaling em duas camadas** — HPA por CPU (2→6 pods) e Cluster Autoscaler (1→4 nodes).

---

### Estrutura de arquivos

```
infra/
├── k8s/                          # Manifests Kubernetes (aplicados pelo módulo deploy)
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret.yaml               # template — valores base64 injetados pelo Terraform
│   ├── deployment.yaml           # template — docker_image injetado pelo Terraform
│   ├── service.yaml              # NodePort 30080
│   ├── hpa.yaml                  # Autoscaling por CPU (2–6 réplicas)
│   └── pdb.yaml                  # PodDisruptionBudget
└── terraform/
    ├── apply.sh                  # Orquestrador local (alternativa aos comandos diretos)
    ├── bootstrap/                # Etapa 1: VPC + EKS + RDS + ECR
    │   ├── main.tf
    │   ├── variables.tf
    │   ├── outputs.tf            # Expõe endpoints e IDs para o módulo deploy
    │   ├── providers.tf          # Somente provider AWS
    │   ├── versions.tf
    │   ├── backend.tf            # Estado em S3: lata-velha/bootstrap/terraform.tfstate
    │   └── terraform.tfvars.example
    ├── deploy/                   # Etapa 2: ALB + app + autoscaler
    │   ├── main.tf               # Lê infra via terraform_remote_state
    │   ├── variables.tf          # Conexão EKS via variáveis (para provider config)
    │   ├── outputs.tf            # app_url
    │   ├── providers.tf          # Providers AWS + kubectl + helm
    │   ├── versions.tf
    │   ├── backend.tf            # Estado em S3: lata-velha/deploy/terraform.tfstate
    │   └── terraform.tfvars.example
    └── modules/                  # Módulos reutilizáveis
        ├── vpc/
        ├── eks/
        ├── rds/
        ├── alb/
        ├── cluster-autoscaler/
        └── app/ 
```

#### Por que dois módulos Terraform separados (`bootstrap` e `deploy`)?

Os providers `kubectl` e `helm` precisam do endpoint do cluster EKS para serem
inicializados — e esse endpoint só existe após o EKS ser criado.

Em um único módulo isso cria um problema: o Terraform tenta configurar os providers
antes de provisionar o cluster, e falha. A solução padrão é dividir em dois módulos
com estados independentes:

- **`bootstrap`** provisiona o EKS (e VPC/RDS/ECR) usando apenas o provider AWS.
- **`deploy`** lê o endpoint do EKS via `terraform_remote_state` e usa esse valor
  para configurar os providers `kubectl` e `helm` — que já encontram o cluster ativo.

Os outputs do `bootstrap` são exportados como variáveis de ambiente (`TF_VAR_`)
para o `deploy`, já que blocos `provider` não aceitam `data sources`.

---

## Explicações

> Decisões de design, workarounds, variáveis de configuração e custos do stack.

### Compatibilidade com AWS Academy

O AWS Academy (Learner Lab) tem restrições de IAM. A tabela abaixo mostra como cada uma é resolvida:

| Restrição                                 | Como é resolvida                                                            |
| ----------------------------------------- | --------------------------------------------------------------------------- |
| `iam:CreateRole` negado                   | Usa a `LabRole` pré-existente para cluster, nodes e ALB                     |
| `iam:CreateOpenIDConnectProvider` negado  | IRSA removido — credenciais injetadas via `kubectl_manifest` (Secret k8s)   |
| IMDS hop limit = 1 nos nodes              | Pods não alcançam instance profile — resolvido pelo secret acima            |
| ARN de assumed-role no Access Entry       | Convertido automaticamente para ARN de IAM role via `sts:GetCallerIdentity` |
| Recursos externos bloqueando VPC no destroy | ALB gerenciado pelo Terraform — destruído em ordem correta sem scripts    |

#### O que não foi possível por causa do Academy

Algumas boas práticas foram **deliberadamente deixadas de fora** porque o Learner Lab as bloqueia:

| Não implementado                          | Por quê (restrição do Academy)                                              | O que foi feito no lugar                          |
| ----------------------------------------- | --------------------------------------------------------------------------- | ------------------------------------------------- |
| **OIDC para credenciais no CI/CD**        | `iam:CreateOpenIDConnectProvider` negado                                    | Secrets estáticos `AWS_*` + `SESSION_TOKEN` (expiram em ~4h, renovados a cada sessão) |
| **HTTPS/TLS no ALB**                      | ACM exige domínio validado e o Route 53 (`CreateHostedZone`) é bloqueado    | ALB serve apenas **HTTP :80**                     |
| **Endpoint do EKS restrito (privado/CIDR)** | O IP do runner do GitHub é dinâmico e as credenciais trocam por sessão     | Endpoint **público** (com acesso via Access Entry) |
| **Chave KMS própria (CMK) no RDS**        | Criação de CMK bloqueada                                                     | Criptografia com a chave gerenciada `aws/rds`     |
| **IRSA (IAM Roles for Service Accounts)** | Depende de OIDC provider, negado                                            | Credenciais injetadas via Secret do Kubernetes    |
| **`deletion_protection` no RDS / backups** | —                                                                          | Mantido desligado de propósito: o lab é descartável |

> Em um ambiente AWS real (fora do Academy), o recomendado seria: OIDC no pipeline, ALB com HTTPS via ACM, endpoint do EKS privado e CMK dedicada — todos viáveis quando há permissões completas de IAM/Route 53/KMS.

### Variáveis do Terraform

<details>
<summary>Tabelas completas de variáveis dos módulos <code>bootstrap</code> e <code>deploy</code> (clique para expandir).</summary>

#### Módulo `bootstrap`

| Variável             | Padrão            | Descrição                           |
| -------------------- | ----------------- | ----------------------------------- |
| `region`             | `us-east-1`       | Região AWS                          |
| `project_name`       | `lata-velha`      | Prefixo de todos os recursos        |
| `environment`        | `dev`             | Tag de ambiente                     |
| `vpc_cidr`           | `10.0.0.0/16`     | CIDR da VPC                         |
| `kubernetes_version` | `1.36`            | Versão do Kubernetes                |
| `node_instance_type` | `t3.small`        | Tipo de EC2 dos nodes               |
| `node_desired_size`  | `2`               | Quantidade inicial de nodes         |
| `node_min_size`      | `1`               | Mínimo de nodes                     |
| `node_max_size`      | `4`               | Máximo de nodes                     |
| `db_name`            | `lata_velha`      | Nome do banco                       |
| `db_username`        | `lata_velha_user` | Usuário do banco                    |
| `db_password`        | —                 | Senha do banco (**obrigatória**)    |
| `rds_instance_class` | `db.t3.micro`     | Classe da instância RDS             |

#### Módulo `deploy`

> `db_username` e `db_password` **não são variáveis do deploy** — são lidos automaticamente do remote state do bootstrap e não precisam ser declarados aqui.

| Variável                | Padrão        | Descrição                                                        |
| ----------------------- | ------------- | ---------------------------------------------------------------- |
| `region`                | `us-east-1`   | Região AWS                                                       |
| `project_name`          | `lata-velha`  | Prefixo dos recursos                                             |
| `environment`           | `dev`         | Tag de ambiente                                                  |
| `docker_image`          | `placeholder` | Imagem ECR (definida automaticamente pelo pipeline)              |
| `mail_username`         | —             | Email remetente Gmail (**obrigatório**)                          |
| `mail_password`         | —             | App password do Gmail (**obrigatório**)                          |
| `dd_api_key`            | —             | Datadog API key para OTLP ingest (**obrigatório**)               |
| `state_bucket`          | —             | Injetado pelo pipeline — não colocar no tfvars                   |
| `cluster_endpoint`      | —             | Injetado pelo pipeline — não colocar no tfvars                   |
| `cluster_ca_data`       | —             | Injetado pelo pipeline — não colocar no tfvars                   |
| `cluster_name`          | —             | Injetado pelo pipeline — não colocar no tfvars                   |
| `aws_access_key_id`     | —             | Injetado pelo pipeline — não colocar no tfvars                   |
| `aws_secret_access_key` | —             | Injetado pelo pipeline — não colocar no tfvars                   |
| `aws_session_token`     | —             | Injetado pelo pipeline — não colocar no tfvars                   |

</details>

### Custo aproximado (us-east-1)

Preços On-Demand verificados em junho/2026 — base de 730 h/mês, sem tráfego de dados.

| Recurso                  | Configuração         | Preço unitário     | Custo/mês       |
| ------------------------ | -------------------- | ------------------ | --------------- |
| EKS control plane        | 1 cluster            | $0,10/h            | ~US$ 73         |
| EC2 nodes (t3.small × 2) | On-Demand, Linux     | $0,0209/h × 2      | ~US$ 31         |
| NAT Gateway              | 1 AZ                 | $0,045/h           | ~US$ 33         |
| ALB                      | 1 ALB (base)         | $0,0225/h + LCU    | ~US$ 16         |
| RDS db.t3.micro          | PostgreSQL, Single-AZ | $0,018/h + 20 GB  | ~US$ 15         |
| **Total estimado**       |                      |                    | **~US$ 168/mês** |

> No **AWS Academy** o crédito é limitado (~US$ 50). Destrua o ambiente com `terraform destroy` (ou `./apply.sh --destroy --auto`) após cada uso.
>
> **Atenção:** versões de Kubernetes em **extended support** elevam o control plane para $0,60/h (~US$ 438/mês). O Kubernetes 1.36 está em suporte padrão no momento desta revisão.

---

## CI/CD (GitHub Actions)

> Configuração do pipeline automatizado que roda em push para `master`.

### Pipeline e configuração de secrets

O pipeline CI/CD (`.github/workflows/main.yml`) roda automaticamente em todo push para `master`. Para funcionar, configure os seguintes valores no repositório GitHub:

`Settings → Secrets and variables → Actions`

#### Secrets

| Nome                    | Valor                 | Onde obter                                                          |
| ----------------------- | --------------------- | ------------------------------------------------------------------- |
| `AWS_ACCESS_KEY_ID`     | `ASIA...`             | AWS Academy → AWS Details                                           |
| `AWS_SECRET_ACCESS_KEY` | `...`                 | AWS Academy → AWS Details                                           |
| `AWS_SESSION_TOKEN`     | `...`                 | AWS Academy → AWS Details                                           |
| `TF_DB_PASSWORD`        | senha do banco        | você define (mínimo 8 chars)                                        |
| `TF_DB_USERNAME`        | `lata_velha_user`     | padrão ou personalize                                               |
| `TF_MAIL_USERNAME`      | `seu@gmail.com`       | sua conta Gmail                                                     |
| `TF_MAIL_PASSWORD`      | `xxxx xxxx xxxx xxxx` | [Senha de App do Google](https://myaccount.google.com/apppasswords) |
| `TF_DD_API_KEY`         | `dda1...`             | Datadog → Organization Settings → API Keys                          |

#### Variables

| Nome               | Valor            |
| ------------------ | ---------------- |
| `AWS_REGION`       | `us-east-1`      |
| `EKS_CLUSTER_NAME` | `lata-velha-eks` |

> **Importante:** As credenciais AWS expiram com cada sessão do Academy. Atualize-as antes de cada push para `master`.

#### Jobs do pipeline

| Job            | Fase | O que faz                                                  |
| -------------- | ---- | ---------------------------------------------------------- |
| `test`         | CI   | compila e roda toda a suíte de testes (mvn)                |
| `tf-bootstrap` | CD   | provisiona VPC + EKS + RDS + ECR (Terraform)               |
| `build`        | CD   | builda a imagem e envia ao ECR com tag = SHA do commit     |
| `tf-deploy`    | CD   | aplica ALB + app + cluster-autoscaler no cluster           |
| `verify`       | CD   | aguarda o rollout e faz smoke test em `/actuator/health`   |

Cada job só roda se o anterior passar (`needs`). As etapas CD só rodam em **push direto para `master`** — em Pull Requests roda apenas o `test`.

---

## Execução local

> O que instalar, como aplicar a infraestrutura manualmente na sua máquina e como validar o resultado.

### Pré-requisitos locais

| Ferramenta   | Versão mínima | Para que serve                                    |
| ------------ | ------------- | ------------------------------------------------- |
| Terraform    | >= 1.10       | provisionar a infra (lock de estado nativo do S3) |
| AWS CLI      | v2            | autenticar providers e rodar `aws eks get-token`  |
| Docker       | qualquer      | build e push da imagem para o ECR                 |
| kubectl      | qualquer      | inspecionar o cluster (opcional, mas recomendado) |
| Java + Maven | Java 21       | rodar os testes                                   |

---

### Comandos Terraform diretos (sem apply.sh)

> Este é o método usado pelo **GitHub Actions** e o recomendado para execução local.
> Configure os arquivos `.tfvars` antes de continuar (veja o [passo de configuração](#configurar-as-variáveis-do-terraform)).

**Instalar o AWS CLI v2** (caso ainda não tenha):

```bash
# Ubuntu / WSL — via instalador oficial
curl "https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip" -o "awscliv2.zip"
sudo apt install -y unzip
unzip awscliv2.zip
sudo ./aws/install
rm -rf awscliv2.zip aws

# Verificar
aws --version   # deve ser aws-cli/2.x
```

### Configurar o AWS CLI

No **AWS Academy**, abra o Learner Lab, clique em **AWS Details** e exporte as credenciais:

```bash
export AWS_ACCESS_KEY_ID=ASIA...
export AWS_SECRET_ACCESS_KEY=...
export AWS_SESSION_TOKEN=...
export AWS_DEFAULT_REGION=us-east-1
```

> As credenciais do Academy expiram em ~4 horas. Repita sempre que a sessão expirar.

Verifique:

```bash
aws sts get-caller-identity
```

### Configurar as variáveis do Terraform

```bash
cd infra/terraform
cp bootstrap/terraform.tfvars.example bootstrap/terraform.tfvars
cp deploy/terraform.tfvars.example    deploy/terraform.tfvars
```

Edite `bootstrap/terraform.tfvars`:

```hcl
db_password = "sua_senha_do_banco"   # mínimo 8 caracteres
```

Edite `deploy/terraform.tfvars`:

```hcl
mail_username = "seu@gmail.com"
mail_password = "xxxx xxxx xxxx xxxx"   # Senha de App do Gmail
dd_api_key    = "dda1..."                 # Datadog API key
```

> **Nunca commite os arquivos `terraform.tfvars`** — já estão no `.gitignore`.

### 3. Rodar o pipeline completo

```bash
./apply.sh --auto
```

O pipeline executa automaticamente em 5 etapas:

```
[1/5] Testes     →  PostgreSQL Docker efêmero + mvn test
[2/5] Bootstrap  →  terraform apply  (VPC + EKS + RDS + ECR)   ~15 min
[3/5] Docker     →  docker build --platform linux/amd64 + push para ECR
[4/5] Deploy     →  terraform apply  (ALB + app + autoscaler)
[5/5] Verificar  →  kubectl rollout status  (timeout 5 min)
```

Tempo total: **~20–30 minutos** na primeira execução.

**Quer pular os testes Maven?**

```bash
./apply.sh --auto --skip-test
```

---

### Verificando o deploy

Os comandos abaixo valem tanto após uma execução **local** (`./apply.sh --auto`) quanto após o **pipeline do GitHub Actions** terminar — o `apply.sh` já executa o smoke test (`[5/5] Verificar`), mas em ambos os casos vale conferir manualmente com `kubectl` e capturar a URL do ALB.

#### 1. Conferir pods e URL do ALB

**Local** (a partir de `infra/terraform/`):

```bash
# Aponta o kubectl para o cluster
aws eks update-kubeconfig --region us-east-1 --name lata-velha-eks

# Verifica se os pods subiram
kubectl get pods -n lata-velha

# Pega a URL do ALB (aguarde ~2 min para ficar ativo)
terraform -chdir=deploy output app_url

# Testa a API
curl http://<DNS_DO_ALB>/actuator/health
```

**GitHub Actions** — sem acesso à máquina rodando o pipeline:

> A URL do ALB não é consultável a partir da sua máquina local — o `terraform state` está no S3 e o backend não é portátil. Pegue a URL direto dos **logs do workflow**:

1. Abra o run em **Actions** → selecione o push que disparou o deploy
2. Job **`CD - Verificação do deploy`** → step **"Smoke test -- health check HTTP"** — a URL fica em `run URL=<DNS_DO_ALB>`

> O `kubectl` também exige credenciais AWS válidas no seu terminal — copie a URL do ALB e use `curl http://<DNS_DO_ALB>/actuator/health` direto do seu navegador ou terminal, sem autenticar no cluster.

#### 2. Teste de carga — HPA & Cluster Autoscaler

Para validar o autoscaling, gere carga suficiente para ultrapassar o limite de CPU (60%) nos pods. O projeto inclui um `locustfile` pronto em `load-test/`.

##### Setup do Locust

**Instalar Python** (caso ainda não tenha):

```bash
# Ubuntu / WSL
sudo apt update
sudo apt install -y python3 python3-venv python3-pip

# Verificar versão
python3 --version   # precisa ser >= 3.9
```

**Criar venv e instalar Locust** (venv evita conflito com PEP 668 do Ubuntu):

```bash
# Cria venv dedicado na raiz do projeto
python3 -m venv .venvs/locust
source .venvs/locust/bin/activate
pip install locust
```

##### Ramp configurada (100 usuários)

Os parâmetros de carga (número de usuários, spawn rate, host) são definidos **na UI do Locust** em `http://localhost:8089` — não há ramp automático no `locustfile.py`. A configuração recomendada para validar o HPA é:

| Parâmetro          | Valor    | Significado                              |
| ------------------ | -------- | ---------------------------------------- |
| **Number of users** | `100`   | pico de usuários simultâneos             |
| **Spawn rate**      | `10`    | usuários novos por segundo (chega a 100 em ~10s) |
| **Host**            | URL do ALB | destino da carga                      |
| **Run time**        | `5m`    | tempo total (ajustar se quiser prolongar) |

> Toda a parametrização é feita pela UI — o `locustfile.py` só define **o que** cada usuário faz (health, login, listar OS).

##### Executar contra a aplicação no EKS

O teste de carga **não roda no GitHub Actions** — o runner não tem rede para o ALB. Rode localmente, com o `kubectl` apontando para o cluster.

Suba a UI do Locust:

```bash
cd load-test
source ../.venvs/locust/bin/activate
locust -f locustfile.py
# Abre http://localhost:8089
```

Na UI do Locust (http://localhost:8089), preencha:

| Campo            | Valor                                                |
| ---------------- | ---------------------------------------------------- |
| **Number of users** | `100`                                              |
| **Spawn rate**     | `100` (usuários por segundo)                       |
| **Host**           | `http://<DNS_DO_ALB>` (pegue com `terraform -chdir=../infra/terraform/deploy output -raw app_url`) |

> **Por que na UI e não via flag?** O `--host` no CLI exige que a URL esteja disponível no momento do comando. Em pipelines, a URL do ALB só existe depois do `terraform apply` e o runner do GitHub Actions não tem rota para a VPC. Pela UI, a URL pode ser colada a qualquer momento — inclusive trocada no meio do teste.

##### Observar o HPA em ação

Em um terceiro terminal, com `kubectl` apontando para o cluster:

```bash
# Watch do HPA — veja réplicas subirem de 2 para até 6
kubectl get hpa -n lata-velha -w

# CPU por pod em tempo real
kubectl top pods -n lata-velha

# Eventos de scaling
kubectl describe hpa lata-velha-api -n lata-velha | tail -20
```

**Comportamento esperado:**

| Carga                     | Réplicas de pods       | Nodes EC2              |
| ------------------------- | ---------------------- | ---------------------- |
| Repouso (0–10 usuários)   | 2 (mínimo)             | 2 (desejado)           |
| **Pico (100 usuários)**   | **até 6 (máx. HPA)**   | **até 4 (máx. ASG)**   |
| Esfriando                 | volta para 2           | volta para 2           |

> O HPA escala os **pods** dentro dos nodes existentes. O Cluster Autoscaler só adiciona um **novo node EC2** quando pods pendentes não cabem nos nodes atuais — isso acontece quando o HPA atinge o limite de 6 réplicas e a carga continua alta.

#### 3. Destruir o ambiente

```bash
./apply.sh --destroy --auto
```

O destroy acontece em ordem reversa:

1. **Deploy** — remove ALB, Target Group, app Kubernetes e autoscaler
2. **Bootstrap** — remove EKS, VPC, RDS, ECR
3. **Bucket S3** — remove o estado remoto do Terraform

> **Destrua o ambiente quando não precisar** — o EKS control plane custa ~US$ 73/mês mesmo sem tráfego.

---

### Flags do apply.sh

```bash
./apply.sh                    # pipeline completo com confirmação interativa
./apply.sh --auto             # pipeline completo sem confirmação
./apply.sh --skip-test        # pula os testes Maven
./apply.sh --auto --skip-test # sem confirmação e sem testes
./apply.sh --destroy          # destroi tudo com confirmação
./apply.sh --destroy --auto   # destroi tudo sem confirmação
```

### O que o `apply.sh` faz por dentro

Espelha o pipeline do GitHub Actions localmente — mesmas etapas, mesma ordem, mesmo backend S3. Útil para depurar um apply que falhou no CI ou iterar sem esperar o runner.

| Etapa    | Comando                              | Saída                                                                 |
| -------- | ------------------------------------ | --------------------------------------------------------------------- |
| setup    | `aws sts get-caller-identity` + `s3api head-bucket` | Descobre o `ACCOUNT_ID`, monta `BUCKET=lata-velha-tfstate-${ACCOUNT_ID}` e cria o bucket com versionamento se não existir. |
| init     | `tf_init` (helper)                   | `terraform init -reconfigure` com `backend-config` apontando para o bucket. |
| env      | `export TF_VAR_aws_*`                | Injeta credenciais AWS em variáveis do Terraform — workaround para o Academy, que não permite IRSA (veja abaixo). |
| `[1/5]`  | `docker run postgres` + `mvn test`   | Sobe um PostgreSQL efêmero na 5432, roda a suíte, derruba o container. Aborta o pipeline se algum teste falhar. |
| `[2/5]`  | `terraform apply` (bootstrap)        | VPC + EKS + RDS + ECR. |
| `[3/5]`  | `docker build` + `push`              | Tag = SHA curto do `git rev-parse` (mesma convenção do CI).         |
| `[4/5]`  | `terraform apply` (deploy)           | ALB + app + autoscaler. Recebe o endpoint do EKS via `TF_VAR_*` lido do output do bootstrap. |
| `[5/5]`  | `kubectl rollout status`             | Aguarda o rollout do `lata-velha-api` (timeout 5 min) e imprime a `app_url`. |

#### `--destroy`

Roda 3 etapas em ordem **reversa** (deploy → bootstrap → bucket) — inverter trava no `VPC has non-foreign dependents` por causa das ENIs do ALB. O bucket só é removido depois de esvaziar todas as versões (`list-object-versions` + `delete-object`) para não deixar lixo quando o versionamento está ativo.

> **Por que exportar `TF_VAR_aws_access_key_id` etc.?** O módulo `cluster-autoscaler` precisa de credenciais AWS dentro do pod para chamar a API de autoscaling. O caminho padrão seria IRSA (OIDC + ServiceAccount), mas o AWS Academy bloqueia `iam:CreateOpenIDConnectProvider`. O `apply.sh` contorna isso lendo as credenciais do ambiente local e injetando-as num Secret do Kubernetes via `kubectl_manifest` — funciona, mas expira junto com a sessão do Academy (~4 h).
