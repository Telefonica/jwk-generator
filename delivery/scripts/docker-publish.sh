#!/usr/bin/env bash

ARG_DEFS=(
  "--image=(.+)"
  "[--registry-server=(.+)]"
  "[--registry-user=(.+)]"
  "[--registry-password=(.+)]"
)

function run() {
  if [[ -n "${REGISTRY_USER:-}" && -n "${REGISTRY_PASSWORD:-}" && -n "${REGISTRY_SERVER:-}" ]]; then
    docker login -u $REGISTRY_USER -p $REGISTRY_PASSWORD $REGISTRY_SERVER
    docker push ${IMAGE}
    docker logout $REGISTRY_SERVER
  else
    docker push ${IMAGE}
  fi
}

source $(dirname $0)/../base.inc
