#!/bin/bash

BRANCH=$(git rev-parse --abbrev-ref HEAD)
ENV=$1

case $ENV in
  local)
    EXPECTED_BRANCH="local"
    COMPOSE_FILE="docker-compose-local.yml"
    ENV_FILE=".env.local"
    ;;
  dev)
    EXPECTED_BRANCH="dev"
    COMPOSE_FILE="docker-compose-dev.yml"
    ENV_FILE=".env.dev"
    ;;
  prod)
    EXPECTED_BRANCH="main"
    COMPOSE_FILE="docker-compose.yml"
    ENV_FILE=".env"
    ;;
  *)
    echo "Uso: ./start.sh [local|dev|prod]"
    exit 1
    ;;
esac

if [ "$BRANCH" != "$EXPECTED_BRANCH" ]; then
  echo "Errore: sei nel branch '$BRANCH', devi essere nel branch '$EXPECTED_BRANCH' per avviare l'ambiente $ENV."
  exit 1
fi

docker compose -f $COMPOSE_FILE --env-file $ENV_FILE up --build