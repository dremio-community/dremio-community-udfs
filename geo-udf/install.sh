#!/usr/bin/env bash
set -euo pipefail
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
JAR="$SCRIPT_DIR/jars/dremio-geo-udf-1.0.0.jar"
DOCKER_CONTAINER="try-dremio"
DREMIO_JAR_DIR="/opt/dremio/jars/3rdparty"
while [[ $# -gt 0 ]]; do
  case "$1" in
    --docker) DOCKER_CONTAINER="$2"; shift 2 ;;
    --jar-dir) DREMIO_JAR_DIR="$2"; shift 2 ;;
    *) echo "Unknown option: $1"; exit 1 ;;
  esac
done
if command -v docker &>/dev/null && docker ps --format '{{.Names}}' | grep -q "^${DOCKER_CONTAINER}$"; then
  echo "Deploying to Docker container: $DOCKER_CONTAINER"
  docker cp "$JAR" "$DOCKER_CONTAINER:$DREMIO_JAR_DIR/dremio-geo-udf-1.0.0.jar"
  docker restart "$DOCKER_CONTAINER"
  echo "Done — Dremio restarting"
else
  echo "Deploying to: $DREMIO_JAR_DIR"
  cp "$JAR" "$DREMIO_JAR_DIR/dremio-geo-udf-1.0.0.jar"
  echo "Done — restart Dremio to load the new JAR"
fi
