#!/bin/bash

BRANCH=$(git rev-parse --abbrev-ref HEAD)
ENV=$1

case $ENV in
  local)
    EXPECTED_BRANCH="local"
    COMPOSE_FILE="docker-compose-local.yml"
    ;;
  dev)
    EXPECTED_BRANCH="dev"
    COMPOSE_FILE="docker-compose-dev.yml"
    ;;
  prod)
    EXPECTED_BRANCH="prod"
    COMPOSE_FILE="docker-compose.yml"
    ;;
  *)
    echo "Uso: ./stop.sh [local|dev|prod]"
    exit 1
    ;;
esac

if [ "$BRANCH" != "$EXPECTED_BRANCH" ]; then
  echo "Errore: sei nel branch '$BRANCH', devi essere nel branch '$EXPECTED_BRANCH' per fermare l'ambiente $ENV."
  exit 1
fi

docker compose -f $COMPOSE_FILE down