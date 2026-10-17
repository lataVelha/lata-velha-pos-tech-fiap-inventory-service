#!/usr/bin/env bash
#
# apply.sh — pipeline local e destroy do deploy da aplicação:
#
#   (padrão)   [1/4] Testes → [2/4] Docker → [3/4] Deploy → [4/4] Verificar
#   --destroy  remove só os recursos da aplicação (Deployment/Service/etc)
#
# Pré-requisitos: repo infra (VPC+EKS+ECR+ALB) e repo infra-db (RDS) já
# aplicados — este script só faz o deploy da aplicação em cima deles.
#
# Uso:
#   ./apply.sh              — pipeline com confirmação interativa
#   ./apply.sh --auto       — pipeline sem confirmação
#   ./apply.sh --skip-test  — pula os testes Maven
#   ./apply.sh --destroy    — remove os recursos da aplicação
#   ./apply.sh --destroy --auto — remove sem confirmação
#
# Pré-requisitos:
#   cp terraform.tfvars.example terraform.tfvars
#   # Edite com suas credenciais de e-mail

set -Eeuo pipefail

# --------------------------- saída no terminal ------------------------------
if [[ -t 1 ]]; then
  C_BLUE=$'\033[1;34m'; C_GREEN=$'\033[1;32m'; C_YELLOW=$'\033[1;33m'; C_RED=$'\033[1;31m'; C_DIM=$'\033[2m'; C_RESET=$'\033[0m'
else
  C_BLUE=''; C_GREEN=''; C_YELLOW=''; C_RED=''; C_DIM=''; C_RESET=''
fi

AUTO=""
SKIP_TESTS=false
DESTROY=false

while [[ $# -gt 0 ]]; do
  case "$1" in
    --auto)                   AUTO="-auto-approve" ;;
    --skip-tests|--skip-test) SKIP_TESTS=true ;;
    --destroy)                DESTROY=true ;;
    *)
      echo "${C_RED}Flag desconhecida: $1${C_RESET}"
      echo "Uso: ./apply.sh [--auto] [--skip-test] [--destroy]"
      exit 1
      ;;
  esac
  shift
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
REGION="${AWS_DEFAULT_REGION:-us-east-1}"
CLUSTER_NAME="lata-velha-eks"
ECR_REPO_NAME="lata-velha"

echo "${C_BLUE}════════════════════════════════════════════════════════════${C_RESET}"
if $DESTROY; then
  echo "${C_YELLOW}  APP — Destruindo Deployment/Service/ConfigMap/Secret/HPA/PDB${C_RESET}"
else
  echo "  APP — Deploy da aplicação"
  echo "${C_DIM}  (pipeline local, igual ao GitHub CI/CD)${C_RESET}"
fi
echo "${C_BLUE}════════════════════════════════════════════════════════════${C_RESET}"

ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
BUCKET="lata-velha-tfstate-${ACCOUNT_ID}"

# Dados do cluster EKS (provisionado pelo repo infra) — via AWS CLI, sem ler
# o Terraform state de outro repo.
export TF_VAR_region="$REGION"
export TF_VAR_state_bucket="$BUCKET"
export TF_VAR_cluster_name="$CLUSTER_NAME"
export TF_VAR_cluster_endpoint=$(aws eks describe-cluster --name "$CLUSTER_NAME" --region "$REGION" --query 'cluster.endpoint' --output text)
export TF_VAR_cluster_ca_data=$(aws eks describe-cluster --name "$CLUSTER_NAME" --region "$REGION" --query 'cluster.certificateAuthority.data' --output text)

tf_init() {
  terraform -chdir="$SCRIPT_DIR" init -reconfigure \
    -backend-config="bucket=${BUCKET}" \
    -backend-config="region=${REGION}"
}

if $DESTROY; then
  echo ""
  echo "${C_BLUE}==> Destruindo recursos da aplicação (Deployment/Service/ConfigMap/Secret/HPA/PDB)...${C_RESET}"
  tf_init
  terraform -chdir="$SCRIPT_DIR" destroy $AUTO
  echo ""
  echo "${C_GREEN}✓ Recursos da aplicação destruídos.${C_RESET}"
  exit 0
fi

# [1/4] Testes
if ! $SKIP_TESTS; then
  echo ""
  echo "${C_BLUE}==> [1/4] Testes${C_RESET} ${C_DIM}— subindo PostgreSQL efêmero...${C_RESET}"
  docker rm -f lata-velha-test-db 2>/dev/null || true
  docker run --rm -d \
    --name lata-velha-test-db \
    -e POSTGRES_DB=lata_velha \
    -e POSTGRES_USER=admin \
    -e POSTGRES_PASSWORD=admin123 \
    -p 5432:5432 \
    postgres:15

  until docker exec lata-velha-test-db pg_isready -U admin 2>/dev/null; do
    sleep 1
  done

  set +e
  (cd "$PROJECT_ROOT" && mvn -B clean test \
    -Dspring.datasource.url=jdbc:postgresql://localhost:5432/lata_velha \
    -Dspring.datasource.username=admin \
    -Dspring.datasource.password=admin123)
  TEST_EXIT=$?
  set -e

  docker stop lata-velha-test-db > /dev/null

  if [[ $TEST_EXIT -ne 0 ]]; then
    echo ""
    echo "${C_RED}✗ [1/4] Testes falharam (código $TEST_EXIT). Deploy cancelado.${C_RESET}"
    exit $TEST_EXIT
  fi
  echo "${C_GREEN}✓ [1/4] Testes concluídos com sucesso.${C_RESET}"
else
  echo ""
  echo "${C_YELLOW}==> [1/4] Testes — pulando (--skip-test)${C_RESET}"
fi

# [2/4] Docker
echo ""
echo "${C_BLUE}==> [2/4] Docker${C_RESET} ${C_DIM}— build e push da imagem${C_RESET}"
ECR_URL=$(aws ecr describe-repositories --repository-names "$ECR_REPO_NAME" --region "$REGION" --query 'repositories[0].repositoryUri' --output text)
GIT_SHA=$(git -C "$PROJECT_ROOT" rev-parse --short HEAD 2>/dev/null || echo "local")
IMAGE="${ECR_URL}:${GIT_SHA}"

aws ecr get-login-password --region "$REGION" \
  | docker login --username AWS --password-stdin "$ECR_URL" > /dev/null

docker build --platform linux/amd64 -t "$IMAGE" "$PROJECT_ROOT"
docker push "$IMAGE"
echo "${C_GREEN}✓ [2/4] Imagem publicada:${C_RESET} $IMAGE"

# [3/4] Deploy
echo ""
echo "${C_BLUE}==> [3/4] Deploy${C_RESET} ${C_DIM}— Deployment + Service + HPA + PDB${C_RESET}"
export TF_VAR_docker_image="$IMAGE"
tf_init
terraform -chdir="$SCRIPT_DIR" apply $AUTO
echo "${C_GREEN}✓ [3/4] Deploy aplicado.${C_RESET}"

# [4/4] Verificar
echo ""
echo "${C_BLUE}==> [4/4] Verificar${C_RESET} ${C_DIM}— aguardando rollout dos pods...${C_RESET}"
aws eks update-kubeconfig --region "$REGION" --name "$CLUSTER_NAME" > /dev/null 2>&1

if command -v kubectl &>/dev/null; then
  kubectl rollout status deployment/lata-velha-api \
    -n lata-velha \
    --timeout=5m
  echo "${C_GREEN}✓ [4/4] Rollout concluído com sucesso.${C_RESET}"
else
  echo "${C_YELLOW}  kubectl não encontrado — verifique manualmente:${C_RESET}"
  echo "  kubectl rollout status deployment/lata-velha-api -n lata-velha"
fi

echo ""
echo "${C_GREEN}✓ APP concluído.${C_RESET}"
# O ALB é interno desde que o API Gateway (repo infra) virou o único ponto
# de entrada — a URL pública de verdade é a do API Gateway, não o DNS do ALB.
API_URL=$(aws apigatewayv2 get-apis --region "$REGION" --query "Items[?Name=='${ECR_REPO_NAME}-app-api'].ApiEndpoint" --output text 2>/dev/null || echo "")
if [[ -n "$API_URL" ]]; then
  echo "  URL: $API_URL"
else
  echo "${C_DIM}  (API Gateway '${ECR_REPO_NAME}-app-api' não encontrado — o infra addons já rodou?)${C_RESET}"
fi
