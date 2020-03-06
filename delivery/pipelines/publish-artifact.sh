#!/usr/bin/env bash

ARG_DEFS=(
  "[--registry-server=(.+)]"
  "[--registry-namespace=(.+)]"
  "[--registry-user=(.+)]"
  "[--registry-password=(.+)]"
  "[--version=(.+)]"
)

function init() {
  SERVICE_NAME=baikal/jwk-generator
  TAG=${VERSION:-snapshot}
  REGISTRY_SERVER=${REGISTRY_SERVER:-}
  # Ensure the registry ends with a slash
  [[ -n "$REGISTRY_SERVER" ]] && [[ $REGISTRY_SERVER =~ .+[^\/]$ ]] && REGISTRY_SERVER=${REGISTRY_SERVER}/
  REGISTRY_NAMESPACE=${REGISTRY_NAMESPACE:-}
  # Ensure the registry namespace ends with a slash
  [[ -n "$REGISTRY_NAMESPACE" ]] && [[ $REGISTRY_NAMESPACE =~ .+[^\/]$ ]] && REGISTRY_NAMESPACE=${REGISTRY_NAMESPACE}/
  IMAGE=${REGISTRY_SERVER}${REGISTRY_NAMESPACE}${SERVICE_NAME}:${TAG}
}

function run() {
  echo "Building '$SERVICE_NAME' Docker image and tagging it as '$IMAGE'..."
  delivery/scripts/docker-package.sh --image=$IMAGE

  if [[ -n "$REGISTRY_SERVER" ]]; then
    [[ -n "${REGISTRY_USER:-}" && -n "${REGISTRY_PASSWORD:-}" ]] && \
      REGISTRY_CREDENTIALS="--registry-user=$REGISTRY_USER --registry-password=$REGISTRY_PASSWORD"
    echo "Pushing Docker image '$IMAGE' to the Docker registry..."
    delivery/scripts/docker-publish.sh --image=$IMAGE --registry-server=$REGISTRY_SERVER ${REGISTRY_CREDENTIALS:-}
  fi
}

source $(dirname $0)/../base.inc
