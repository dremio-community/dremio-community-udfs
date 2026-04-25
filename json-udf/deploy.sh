#!/usr/bin/env bash
# deploy.sh — Build and deploy the Dremio JSON UDF to a running Dremio instance
#
# USAGE
#   ./deploy.sh [--docker CONTAINER] [--local DREMIO_HOME] [--k8s POD]
#
# OPTIONS
#   --docker CONTAINER   Deploy to a running Docker container (default: try-dremio)
#   --local  DREMIO_HOME Deploy to a bare-metal Dremio installation (requires local Java+Maven)
#   --k8s    POD         Deploy to a Kubernetes pod
#   --namespace NS (-n)  Kubernetes namespace (k8s mode only)
#   --dry-run            Build only, do not deploy or restart
#   -h, --help           Show this help

set -euo pipefail
export PATH="/usr/local/bin:/usr/bin:/bin:$PATH"

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
MODE="docker"
TARGET="try-dremio"
K8S_NS=""
DRY_RUN=false
JAR_NAME="dremio-json-udf-1.0.0.jar"
DREMIO_3RDPARTY="/opt/dremio/jars/3rdparty"

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; RED='\033[0;31m'
CYAN='\033[0;36m'; BOLD='\033[1m'; RESET='\033[0m'
ok()   { echo -e "    ${GREEN}✓${RESET}  $*"; }
warn() { echo -e "    ${YELLOW}⚠${RESET}  $*"; }
err()  { echo -e "    ${RED}✗${RESET}  $*" >&2; exit 1; }
info() { echo -e "    ${CYAN}→${RESET}  $*"; }
step() { echo -e "\n${BOLD}[$1]${RESET} $2"; }

while [[ $# -gt 0 ]]; do
  case "$1" in
    --docker)     MODE="docker"; TARGET="$2"; shift 2 ;;
    --local)      MODE="local";  TARGET="$2"; shift 2 ;;
    --k8s)        MODE="k8s";    TARGET="$2"; shift 2 ;;
    --namespace|-n) K8S_NS="$2"; shift 2 ;;
    --dry-run)    DRY_RUN=true;  shift ;;
    -h|--help)    grep "^#" "$0" | grep -v '^#!/' | sed 's/^# \{0,1\}//'; exit 0 ;;
    *) err "Unknown option: $1" ;;
  esac
done

NS_ARG="${K8S_NS:+--namespace $K8S_NS}"

echo ""
echo -e "${BOLD}${CYAN}════════════════════════════════════════════${RESET}"
echo -e "${BOLD}${CYAN}   Dremio JSON UDF — Build & Deploy${RESET}"
echo -e "${BOLD}${CYAN}════════════════════════════════════════════${RESET}"
echo ""
info "Mode   : $MODE"
info "Target : $TARGET"

step 1 "Building inside container (uses Dremio's own JARs + Maven)"

case "$MODE" in
  docker)
    docker exec -u root "$TARGET" bash -c "rm -rf /tmp/json-udf-build" 2>/dev/null || true
    docker cp "$SCRIPT_DIR/." "${TARGET}:/tmp/json-udf-build"
    docker exec -u root "$TARGET" bash -c "
      cd /tmp/json-udf-build
      mvn package -q -DskipTests 2>&1
    " || err "Maven build failed"
    ok "Build successful"

    # Copy JAR back to host jars/ folder
    docker cp "${TARGET}:/tmp/json-udf-build/jars/${JAR_NAME}" "${SCRIPT_DIR}/jars/${JAR_NAME}" 2>/dev/null || true
    ok "JAR saved to jars/${JAR_NAME}"
    ;;
  local)
    cd "$SCRIPT_DIR" && mvn package -q -DskipTests || err "Maven build failed"
    ok "Build successful"
    ;;
  k8s)
    kubectl cp $NS_ARG "$SCRIPT_DIR" "${TARGET}:/tmp/json-udf-build"
    kubectl exec $NS_ARG "$TARGET" -- bash -c "cd /tmp/json-udf-build && mvn package -q -DskipTests"
    ok "Build successful"
    ;;
esac

if $DRY_RUN; then
  warn "Dry run — skipping deploy and restart"
  exit 0
fi

step 2 "Deploying JAR to $MODE target"

case "$MODE" in
  docker)
    docker exec -u root "$TARGET" bash -c "
      cp /tmp/json-udf-build/jars/${JAR_NAME} ${DREMIO_3RDPARTY}/${JAR_NAME}
      chmod 644 ${DREMIO_3RDPARTY}/${JAR_NAME}
    "
    ok "JAR deployed to ${DREMIO_3RDPARTY}/"
    ;;
  local)
    install -m 644 "${SCRIPT_DIR}/jars/${JAR_NAME}" "${TARGET}/jars/3rdparty/${JAR_NAME}"
    ok "JAR deployed to ${TARGET}/jars/3rdparty/"
    ;;
  k8s)
    kubectl cp $NS_ARG "${SCRIPT_DIR}/jars/${JAR_NAME}" "${TARGET}:${DREMIO_3RDPARTY}/${JAR_NAME}"
    ok "JAR deployed to pod ${TARGET}"
    ;;
esac

step 3 "Restarting Dremio"

case "$MODE" in
  docker)
    docker restart "$TARGET" > /dev/null
    info "Waiting for Dremio to start..."
    for i in $(seq 1 20); do
      HTTP=$(curl -s -o /dev/null -w "%{http_code}" "http://localhost:9047/apiv2/info" 2>/dev/null || true)
      if [[ "$HTTP" == "200" || "$HTTP" == "404" ]]; then
        echo ""; ok "Dremio is up"; break
      fi
      printf "."; sleep 3
    done
    echo ""
    ;;
  local)
    "${TARGET}/bin/dremio" restart 2>/dev/null || warn "Restart Dremio manually to load the UDF."
    ;;
  k8s)
    kubectl exec $NS_ARG "$TARGET" -- /opt/dremio/bin/dremio restart 2>/dev/null \
      || warn "Restart the pod manually to load the UDF."
    ;;
esac

echo ""
echo -e "${GREEN}${BOLD}JSON UDF deployed successfully.${RESET}"
echo ""
echo -e "  Test in Dremio SQL:"
echo -e "    ${BOLD}SELECT JSON_EXTRACT_STR('{\"name\":\"alice\"}', 'name')${RESET}"
echo -e "    ${BOLD}SELECT JSON_IS_VALID('{\"ok\":true}')${RESET}"
echo -e "    ${BOLD}SELECT JSON_KEYS('{\"a\":1,\"b\":2}')${RESET}"
echo ""
