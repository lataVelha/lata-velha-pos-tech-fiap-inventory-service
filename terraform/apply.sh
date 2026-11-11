#!/usr/bin/env bash
#
# apply.sh — deploy (ou destroy) completo do inventory-service na AWS:
#
#   (padrão)   [1/5] Testes → [2/5] ECR + Docker → [3/5] Terraform apply →
#              [4/5] Rollout → [5/5] Smoke test
#   --destroy  remove Deployment/Service/ConfigMap/Secret/HPA/PDB e o ECR
#              deste serviço (nunca toca no `app` nem no inventory/billing)
#
# Pré-requisitos (aplicados pelo mono, nesta ordem):
#   infra bootstrap → infra addons → infra-db → lambda
#
# Uso:
#   ./apply.sh                  — deploy com confirmação interativa
#   ./apply.sh --auto           — deploy sem confirmação
#   ./apply.sh --skip-tests     — pula os testes Maven
#   ./apply.sh --destroy        — remove tudo, com confirmação
#   ./apply.sh --destroy --auto — remove tudo, sem confirmação
#
# Variáveis: TF_VAR_mail_username/TF_VAR_mail_password (obrigatórias) e
#            TF_VAR_dd_api_key (opcional), ou terraform.tfvars.

set -Eeuo pipefail

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
      echo "Uso: ./apply.sh [--auto] [--skip-tests] [--destroy]"
      exit 1
      ;;
  esac
  shift
done

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"
REGION="${AWS_DEFAULT_REGION:-us-east-1}"
CLUSTER_NAME="${CLUSTER_NAME:-lata-velha-eks}"
SERVICE="lata-velha-inventory"
NAMESPACE="lata-velha-inventory"
ECR_TARGETS=(-target=aws_ecr_repository.inventory_service -target=aws_ecr_lifecycle_policy.inventory_service)

echo "${C_BLUE}════════════════════════════════════════════════════════════${C_RESET}"
if $DESTROY; then
  echo "${C_YELLOW}  OS-SERVICE — Destruindo Deployment, Service, ConfigMaps e ECR (isolado)${C_RESET}"
else
  echo "  OS-SERVICE — Deploy do inventory-service"
fi
echo "${C_BLUE}════════════════════════════════════════════════════════════${C_RESET}"

ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
BUCKET="lata-velha-tfstate-${ACCOUNT_ID}"

export TF_VAR_region="$REGION"
export TF_VAR_cluster_name="$CLUSTER_NAME"
export TF_VAR_cluster_endpoint=$(aws eks describe-cluster --name "$CLUSTER_NAME" --region "$REGION" --query 'cluster.endpoint' --output text)
export TF_VAR_cluster_ca_data=$(aws eks describe-cluster --name "$CLUSTER_NAME" --region "$REGION" --query 'cluster.certificateAuthority.data' --output text)
export TF_VAR_state_bucket="$BUCKET"
# SHA local vira a tag service.version nos spans (fallback: 0.0.1).
export TF_VAR_service_version="$(git -C "$PROJECT_ROOT" rev-parse --short HEAD 2>/dev/null || echo 0.0.1)"

terraform -chdir="$SCRIPT_DIR" init -reconfigure \
  -backend-config="bucket=${BUCKET}" \
  -backend-config="region=${REGION}"

if $DESTROY; then
  echo ""
  echo "${C_BLUE}==> Destruindo recursos do inventory-service...${C_RESET}"
  TF_VAR_docker_image="destroy" \
  TF_VAR_mail_username="destroy" \
  TF_VAR_mail_password="destroy" \
  TF_VAR_dd_api_key="destroy" \
    terraform -chdir="$SCRIPT_DIR" destroy $AUTO
  echo ""
  echo "${C_GREEN}✓ inventory-service destruído (o app e os outros serviços não foram tocados).${C_RESET}"
  exit 0
fi

# [1/5] Testes
if ! $SKIP_TESTS; then
  echo ""
  echo "${C_BLUE}==> [1/5] Testes${C_RESET}"
  (cd "$PROJECT_ROOT" && ./mvnw -B clean test -Dspring.profiles.active=test)
  echo "${C_GREEN}✓ [1/5] Testes concluídos.${C_RESET}"
else
  echo ""
  echo "${C_YELLOW}==> [1/5] Testes — pulando (--skip-tests)${C_RESET}"
fi

# [2/5] ECR + Docker — o repositório é deste stack; o apply direcionado
# garante que ele exista antes do push.
echo ""
echo "${C_BLUE}==> [2/5] ECR + Docker${C_RESET} ${C_DIM}— build e push da imagem${C_RESET}"
TF_VAR_docker_image="pending" terraform -chdir="$SCRIPT_DIR" apply $AUTO "${ECR_TARGETS[@]}"
ECR_URL=$(terraform -chdir="$SCRIPT_DIR" output -raw ecr_repository_url)
GIT_SHA=$(git -C "$PROJECT_ROOT" rev-parse --short HEAD 2>/dev/null || echo "local")
if [[ -n "$(git -C "$PROJECT_ROOT" status --porcelain 2>/dev/null)" ]]; then
  GIT_SHA="${GIT_SHA}-dirty-$(date +%Y%m%d%H%M%S)"
fi
IMAGE="${ECR_URL}:${GIT_SHA}"

aws ecr get-login-password --region "$REGION" \
  | docker login --username AWS --password-stdin "${ECR_URL%%/*}" > /dev/null

docker build --platform linux/amd64 -t "$IMAGE" "$PROJECT_ROOT"
docker push "$IMAGE"
echo "${C_GREEN}✓ [2/5] Imagem publicada:${C_RESET} $IMAGE"

# [3/5] Deploy
echo ""
echo "${C_BLUE}==> [3/5] Terraform apply${C_RESET} ${C_DIM}— namespace/configmap/secret/deployment/service/hpa/pdb${C_RESET}"
export TF_VAR_docker_image="$IMAGE"
terraform -chdir="$SCRIPT_DIR" apply $AUTO
echo "${C_GREEN}✓ [3/5] Deploy aplicado.${C_RESET}"

# [4/5] Rollout
echo ""
echo "${C_BLUE}==> [4/5] Rollout${C_RESET}"
aws eks update-kubeconfig --region "$REGION" --name "$CLUSTER_NAME" > /dev/null 2>&1
if command -v kubectl &>/dev/null; then
  kubectl rollout status "deployment/${SERVICE}" -n "$NAMESPACE" --timeout=5m
  echo "${C_GREEN}✓ [4/5] Rollout concluído.${C_RESET}"
else
  echo "${C_YELLOW}  kubectl não encontrado — verifique manualmente:${C_RESET}"
  echo "  kubectl rollout status deployment/${SERVICE} -n ${NAMESPACE}"
fi

# [5/5] Smoke test
echo ""
echo "${C_BLUE}==> [5/5] Smoke test${C_RESET} ${C_DIM}— GET /actuator/health${C_RESET}"
SERVICE="$SERVICE" NAMESPACE="$NAMESPACE" HEALTH_PATH="/actuator/health" \
  bash "$PROJECT_ROOT/scripts/smoke-test.sh"

echo ""
echo "${C_GREEN}✓ OS-SERVICE concluído.${C_RESET}"
