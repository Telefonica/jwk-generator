#!/usr/bin/env bash

ARG_DEFS=(
  "--image=(.+)"
)

function run() {
  echo "Build docker image $IMAGE"

  docker build \
      --tag ${IMAGE} \
      --file $(dirname $0)/../docker/Dockerfile \
      $(dirname $0)/../..
}

source $(dirname $0)/../base.inc
