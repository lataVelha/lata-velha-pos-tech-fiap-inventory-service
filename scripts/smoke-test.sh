#!/usr/bin/env bash
#
# smoke-test.sh — verificação pós-deploy padronizada (Story 10.6).
#
# 1. espera o rollout do Deployment;
# 2. abre um port-forward para o Service;
# 3. faz GET no health path com até ATTEMPTS tentativas;
# 4. publica um resumo em $GITHUB_STEP_SUMMARY (quando existir).
#
# Uso:
#   SERVICE=lata-velha-os NAMESPACE=lata-velha-os bash scripts/smoke-test.sh
#
# Variáveis: SERVICE, NAMESPACE, HEALTH_PATH (default /actuator/health),
#            ATTEMPTS (10), RETRY_SECONDS (15), LOCAL_PORT (18080),
#            ROLLOUT_TIMEOUT (10m).

set -Eeuo pipefail

SERVICE="${SERVICE:?defina SERVICE (nome do Deployment/Service)}"
NAMESPACE="${NAMESPACE:?defina NAMESPACE}"
HEALTH_PATH="${HEALTH_PATH:-/actuator/health}"
ATTEMPTS="${ATTEMPTS:-10}"
RETRY_SECONDS="${RETRY_SECONDS:-15}"
LOCAL_PORT="${LOCAL_PORT:-18080}"
ROLLOUT_TIMEOUT="${ROLLOUT_TIMEOUT:-10m}"
KUBECTL="${KUBECTL:-kubectl}"

if [[ -t 1 ]]; then
  C_GREEN=$'\033[1;32m'; C_RED=$'\033[1;31m'; C_YELLOW=$'\033[1;33m'; C_RESET=$'\033[0m'
else
  C_GREEN=''; C_RED=''; C_YELLOW=''; C_RESET=''
fi

# Diagnóstico do rollout: sem isso, um pod em CrashLoop/Pending só aparece
# como "rollout status" falhou (ou como o erro enganoso do provider kubectl:
# "client rate limiter Wait returned an error: context deadline exceeded").
dump_diagnostics() {
  echo "--- deployment/${SERVICE} ---" >&2
  ${KUBECTL} -n "${NAMESPACE}" get deployment "${SERVICE}" -o wide >&2 || true
  ${KUBECTL} -n "${NAMESPACE}" describe deployment "${SERVICE}" >&2 || true

  echo "--- pods ---" >&2
  ${KUBECTL} -n "${NAMESPACE}" get pods -l "app=${SERVICE}" -o wide >&2 || true
  ${KUBECTL} -n "${NAMESPACE}" describe pods -l "app=${SERVICE}" >&2 || true

  echo "--- events (últimos 40) ---" >&2
  ${KUBECTL} -n "${NAMESPACE}" get events --sort-by=.lastTimestamp 2>/dev/null | tail -40 >&2 || true

  local pod
  for pod in $(${KUBECTL} -n "${NAMESPACE}" get pods -l "app=${SERVICE}" -o name 2>/dev/null || true); do
    echo "--- logs: ${pod} ---" >&2
    ${KUBECTL} -n "${NAMESPACE}" logs "${pod}" --all-containers --tail=200 >&2 || true
    echo "--- logs (previous): ${pod} ---" >&2
    ${KUBECTL} -n "${NAMESPACE}" logs "${pod}" --all-containers --previous --tail=200 >&2 || true
  done
}

echo "${C_YELLOW}==> rollout status deployment/${SERVICE} -n ${NAMESPACE} (timeout ${ROLLOUT_TIMEOUT})${C_RESET}"
if ! ${KUBECTL} rollout status "deployment/${SERVICE}" -n "${NAMESPACE}" --timeout="${ROLLOUT_TIMEOUT}"; then
  echo "${C_RED}✗ rollout nao concluiu — diagnostico abaixo${C_RESET}" >&2
  dump_diagnostics
  exit 1
fi

PF_PID=""
cleanup() {
  if [[ -n "${PF_PID}" ]]; then
    kill "${PF_PID}" 2>/dev/null || true
    wait "${PF_PID}" 2>/dev/null || true
  fi
}
trap cleanup EXIT

echo "${C_YELLOW}==> port-forward svc/${SERVICE} -n ${NAMESPACE} → localhost:${LOCAL_PORT}${C_RESET}"
${KUBECTL} port-forward -n "${NAMESPACE}" "svc/${SERVICE}" "${LOCAL_PORT}:80" >/tmp/smoke-port-forward.log 2>&1 &
PF_PID=$!
sleep 3

URL="http://127.0.0.1:${LOCAL_PORT}${HEALTH_PATH}"
STATUS="000"
for ((i = 1; i <= ATTEMPTS; i++)); do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" "${URL}" 2>/dev/null || echo "000")
  if [[ "${STATUS}" == "200" ]]; then
    echo "${C_GREEN}✓ health OK (HTTP ${STATUS}) — serviço=${SERVICE} namespace=${NAMESPACE} url=${URL}${C_RESET}"
    if [[ -n "${GITHUB_STEP_SUMMARY:-}" ]]; then
      {
        echo ""
        echo "### Smoke test — ${SERVICE}"
        echo ""
        echo "| item | valor |"
        echo "|---|---|"
        echo "| namespace | \`${NAMESPACE}\` |"
        echo "| deployment | \`${SERVICE}\` |"
        echo "| url testada | \`${URL}\` |"
        echo "| status | \`${STATUS}\` |"
        echo "| tentativas | ${i}/${ATTEMPTS} |"
      } >> "${GITHUB_STEP_SUMMARY}"
    fi
    exit 0
  fi
  echo "Tentativa ${i}/${ATTEMPTS}: HTTP ${STATUS} — aguardando ${RETRY_SECONDS}s..."
  sleep "${RETRY_SECONDS}"
done

echo "${C_RED}✗ smoke test falhou — serviço=${SERVICE} namespace=${NAMESPACE} url=${URL} status=${STATUS}${C_RESET}" >&2
echo "--- port-forward log ---" >&2
cat /tmp/smoke-port-forward.log >&2 || true
echo "--- pods ---" >&2
${KUBECTL} get pods -n "${NAMESPACE}" -l "app=${SERVICE}" >&2 || true
exit 1
