#!/usr/bin/env bash
# install.sh — Install one or more Dremio Community UDFs in a single step
#
# Usage:
#   ./install.sh [OPTIONS]
#
# Options:
#   --udfs      LIST    Comma-separated UDF names, e.g. finance,crypto,datetime
#                       Available: crypto, datetime, finance, geo, json, ml, pii, text-similarity, vector
#   --all               Install every UDF library
#   --docker    NAME    Dremio Docker container name (default: try-dremio)
#   --local     PATH    Bare-metal Dremio install dir (e.g. /opt/dremio)
#   --k8s       POD     Kubernetes pod name (e.g. dremio-0)
#   --no-restart        Skip Dremio restart (useful when chaining installs)
#   --help              Show this help
#
# Examples:
#   ./install.sh --all
#   ./install.sh --udfs finance,crypto,datetime
#   ./install.sh --udfs geo --docker my-dremio
#   ./install.sh --all --local /opt/dremio --no-restart

set -euo pipefail

GREEN='\033[0;32m'; YELLOW='\033[1;33m'; CYAN='\033[0;36m'; BOLD='\033[1m'; RESET='\033[0m'
ok()     { echo -e "  ${GREEN}✓${RESET} $*"; }
warn()   { echo -e "  ${YELLOW}⚠${RESET} $*"; }
info()   { echo -e "  ${CYAN}→${RESET} $*"; }
header() { echo -e "\n${BOLD}$*${RESET}"; }

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
DREMIO_JAR_DIR="/opt/dremio/jars/3rdparty"
ALL_UDFS="crypto datetime finance geo json ml pii text-similarity vector"

CONTAINER=""
LOCAL_DIR=""
K8S_POD=""
RESTART=true
SELECTED_UDFS=""

usage() { sed -n '3,21p' "$0" | sed 's/^# *//'; exit 0; }

while [[ $# -gt 0 ]]; do
  case $1 in
    --udfs)       SELECTED_UDFS="$2";                 shift 2 ;;
    --all)        SELECTED_UDFS="$ALL_UDFS";          shift ;;
    --docker)     CONTAINER="$2";                     shift 2 ;;
    --local)      LOCAL_DIR="$2";                     shift 2 ;;
    --k8s)        K8S_POD="$2";                       shift 2 ;;
    --no-restart) RESTART=false;                      shift ;;
    --help|-h)    usage ;;
    *) echo "Unknown option: $1"; exit 1 ;;
  esac
done

[[ -z "$SELECTED_UDFS" ]] && { echo "No UDFs specified. Use --udfs LIST or --all."; echo "Run ./install.sh --help for usage."; exit 1; }
[[ -z "$CONTAINER" && -z "$LOCAL_DIR" && -z "$K8S_POD" ]] && CONTAINER="try-dremio"

DOCKER=$(command -v docker 2>/dev/null || echo "/Applications/Docker.app/Contents/Resources/bin/docker")

# Validate target
if [[ -n "$CONTAINER" ]]; then
  $DOCKER ps --format '{{.Names}}' | grep -q "^${CONTAINER}$" \
    || { echo "ERROR: Container '$CONTAINER' is not running"; exit 1; }
elif [[ -n "$LOCAL_DIR" ]]; then
  [[ -d "$LOCAL_DIR/jars/3rdparty" ]] \
    || { echo "ERROR: '$LOCAL_DIR/jars/3rdparty' not found"; exit 1; }
elif [[ -n "$K8S_POD" ]]; then
  kubectl get pod "$K8S_POD" &>/dev/null \
    || { echo "ERROR: Pod '$K8S_POD' not found"; exit 1; }
fi

header "Dremio Community UDFs — Installer"
echo ""

INSTALLED=""
SKIPPED=""

# Convert comma-separated list to space-separated for iteration
UDFS_TO_INSTALL=$(echo "$SELECTED_UDFS" | tr ',' ' ')

for udf in $UDFS_TO_INSTALL; do
  # Validate name
  if ! echo "$ALL_UDFS" | grep -qw "$udf"; then
    warn "$udf — unknown UDF name (skipping). Available: $ALL_UDFS"
    SKIPPED="$SKIPPED $udf"
    continue
  fi

  dir="${udf}-udf"
  jar_path=$(find "$SCRIPT_DIR/$dir/jars" -name "*.jar" 2>/dev/null | head -1)

  if [[ -z "$jar_path" ]]; then
    warn "$udf — no JAR found in $dir/jars/ (skipping)"
    SKIPPED="$SKIPPED $udf"
    continue
  fi

  jar_name=$(basename "$jar_path")
  info "Installing $udf ($jar_name)..."

  if [[ -n "$CONTAINER" ]]; then
    $DOCKER cp "$jar_path" "${CONTAINER}:${DREMIO_JAR_DIR}/${jar_name}"
  elif [[ -n "$LOCAL_DIR" ]]; then
    cp "$jar_path" "${LOCAL_DIR}/jars/3rdparty/${jar_name}"
  elif [[ -n "$K8S_POD" ]]; then
    kubectl cp "$jar_path" "${K8S_POD}:${DREMIO_JAR_DIR}/${jar_name}"
  fi

  ok "$udf → ${DREMIO_JAR_DIR}/${jar_name}"
  INSTALLED="$INSTALLED $udf"
done

echo ""
INSTALLED=$(echo "$INSTALLED" | xargs)
SKIPPED=$(echo "$SKIPPED" | xargs)

if [[ -z "$INSTALLED" ]]; then
  warn "Nothing was installed."
  exit 1
fi

INSTALLED_COUNT=$(echo "$INSTALLED" | wc -w | xargs)
ok "Installed $INSTALLED_COUNT UDF(s): $INSTALLED"
[[ -n "$SKIPPED" ]] && warn "Skipped: $SKIPPED"

if $RESTART; then
  echo ""
  info "Restarting Dremio..."
  if [[ -n "$CONTAINER" ]]; then
    $DOCKER restart "$CONTAINER"
    info "Waiting for Dremio to come up (up to 90s)..."
    for i in $(seq 1 18); do
      STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:9047/ 2>/dev/null || echo "000")
      [[ "$STATUS" == "200" ]] && { ok "Dremio is up"; break; }
      sleep 5
    done
  elif [[ -n "$LOCAL_DIR" ]]; then
    "${LOCAL_DIR}/bin/dremio" restart
  elif [[ -n "$K8S_POD" ]]; then
    kubectl exec "$K8S_POD" -- /opt/dremio/bin/dremio restart
  fi
else
  warn "Skipping restart (--no-restart). Restart Dremio manually to activate the UDFs."
fi

echo ""
ok "Done. Installed UDFs are now available in Dremio SQL."
echo ""
echo "  Quick tests:"
for udf in $INSTALLED; do
  case $udf in
    finance)         echo "    SELECT fin_pmt(0.005, 360, 200000)        -- → -1199.10" ;;
    crypto)          echo "    SELECT CRYPTO_SHA256('hello')" ;;
    datetime)        echo "    SELECT DT_FISCAL_QUARTER(CURRENT_DATE, 1)" ;;
    geo)             echo "    SELECT ST_AsText(ST_Point(-122.42, 37.78))" ;;
    json)            echo "    SELECT JSON_EXTRACT_STR('{\"name\":\"alice\"}', 'name')" ;;
    ml)              echo "    SELECT ML_SIGMOID(0.5)                    -- → 0.6225" ;;
    pii)             echo "    SELECT MASK_EMAIL('user@example.com')" ;;
    text-similarity) echo "    SELECT TEXT_JARO_WINKLER('dremio', 'dremio')" ;;
    vector)          echo "    SELECT COSINE_SIMILARITY('[1,0,0]', '[1,0,0]')" ;;
  esac
done
echo ""
