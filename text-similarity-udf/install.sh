#!/usr/bin/env bash
# install.sh — Build and deploy the Dremio Text Similarity UDF to a running Dremio instance
#
# Usage:
#   ./install.sh [OPTIONS]
#
# Options:
#   --docker    NAME    Dremio Docker container name (default: try-dremio)
#   --local     PATH    Bare-metal Dremio install dir (e.g. /opt/dremio)
#   --k8s       POD     Kubernetes pod name (e.g. dremio-0)
#   --prebuilt          Use JAR from jars/ instead of building from source
#   --no-restart        Don't restart Dremio after installing
#   --help              Show this help

set -euo pipefail

GREEN='\033[0;32m'; CYAN='\033[0;36m'; RESET='\033[0m'
ok()   { echo -e "  ${GREEN}✓${RESET} $*"; }
info() { echo -e "  ${CYAN}→${RESET} $*"; }

CONTAINER=""
LOCAL_DIR=""
K8S_POD=""
PREBUILT=false
RESTART=true
DREMIO_JAR_DIR="/opt/dremio/jars/3rdparty"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"

while [[ $# -gt 0 ]]; do
  case $1 in
    --docker)      CONTAINER="$2";    shift 2 ;;
    --local)       LOCAL_DIR="$2";    shift 2 ;;
    --k8s)         K8S_POD="$2";      shift 2 ;;
    --prebuilt)    PREBUILT=true;     shift ;;
    --no-restart)  RESTART=false;     shift ;;
    --help|-h)     sed -n '3,12p' "$0" | sed 's/^# *//'; exit 0 ;;
    *) echo "Unknown option: $1"; exit 1 ;;
  esac
done

# Default to Docker try-dremio if nothing specified
if [[ -z "$CONTAINER" && -z "$LOCAL_DIR" && -z "$K8S_POD" ]]; then
  CONTAINER="try-dremio"
fi

# Build or use prebuilt JAR
if $PREBUILT; then
  JAR_PATH=$(find "$SCRIPT_DIR/jars" -name "dremio-text-similarity-udf-*.jar" | head -1)
  [[ -z "$JAR_PATH" ]] && { echo "ERROR: No prebuilt JAR found in jars/"; exit 1; }
  info "Using prebuilt JAR: $(basename "$JAR_PATH")"
else
  info "Building from source..."
  cd "$SCRIPT_DIR"
  mvn package -DskipTests -q
  JAR_PATH=$(find "$SCRIPT_DIR/target" -name "dremio-text-similarity-udf-*.jar" ! -name "*sources*" | head -1)
  [[ -z "$JAR_PATH" ]] && { echo "ERROR: Build failed — no JAR in target/"; exit 1; }
  ok "Build complete: $(basename "$JAR_PATH")"
fi

JAR_NAME=$(basename "$JAR_PATH")

# Deploy
if [[ -n "$CONTAINER" ]]; then
  DOCKER=$(command -v docker 2>/dev/null || echo "/Applications/Docker.app/Contents/Resources/bin/docker")
  [[ ! -x "$DOCKER" ]] && { echo "ERROR: docker not found"; exit 1; }
  $DOCKER ps --format '{{.Names}}' | grep -q "^${CONTAINER}$" || { echo "ERROR: Container '$CONTAINER' is not running"; exit 1; }
  info "Deploying to Docker container '$CONTAINER'..."
  $DOCKER cp "$JAR_PATH" "${CONTAINER}:${DREMIO_JAR_DIR}/${JAR_NAME}"
  ok "Copied to ${DREMIO_JAR_DIR}/${JAR_NAME}"
  if $RESTART; then
    info "Restarting Dremio..."
    $DOCKER exec -u dremio "$CONTAINER" /opt/dremio/bin/dremio stop 2>/dev/null || true
    sleep 3
    $DOCKER exec -u dremio "$CONTAINER" /opt/dremio/bin/dremio start
    info "Waiting for Dremio to come up (up to 90s)..."
    for i in $(seq 1 18); do
      STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9047/ 2>/dev/null || echo "000")
      [[ "$STATUS" == "200" ]] && { ok "Dremio is up"; break; }
      sleep 5
    done
  fi

elif [[ -n "$LOCAL_DIR" ]]; then
  cp "$JAR_PATH" "${LOCAL_DIR}/jars/3rdparty/${JAR_NAME}"
  ok "Copied to ${LOCAL_DIR}/jars/3rdparty/${JAR_NAME}"
  if $RESTART; then
    info "Restarting Dremio..."
    "${LOCAL_DIR}/bin/dremio" restart
  fi

elif [[ -n "$K8S_POD" ]]; then
  kubectl cp "$JAR_PATH" "${K8S_POD}:${DREMIO_JAR_DIR}/${JAR_NAME}"
  ok "Copied to ${K8S_POD}:${DREMIO_JAR_DIR}/${JAR_NAME}"
  if $RESTART; then
    info "Restarting Dremio pod..."
    kubectl exec "$K8S_POD" -- /opt/dremio/bin/dremio restart
  fi
fi

echo ""
ok "Text Similarity UDF installed successfully"
echo ""
echo "  Test with:"
echo "    SELECT TEXT_JARO_WINKLER('Robert', 'Rupert')"
echo "    SELECT TEXT_LEVENSHTEIN('kitten', 'sitting')"
echo "    SELECT TEXT_TRIGRAM_SIMILARITY('hello world', 'hello there')"
echo "    SELECT TEXT_FUZZY_MATCH('John Smith', 'Jon Smyth', 0.85)"
echo ""
