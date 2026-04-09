param([string]$ENV)

$BRANCH = git rev-parse --abbrev-ref HEAD

switch ($ENV) {
  "local" { $EXPECTED_BRANCH = "local"; $COMPOSE_FILE = "docker-compose-local.yml" }
  "dev"   { $EXPECTED_BRANCH = "dev";   $COMPOSE_FILE = "docker-compose-dev.yml"   }
  "prod"  { $EXPECTED_BRANCH = "prod";  $COMPOSE_FILE = "docker-compose.yml"       }
  default { Write-Host "Uso: .\stop.ps1 [local|dev|prod]"; exit 1 }
}

if ($BRANCH -ne $EXPECTED_BRANCH) {
  Write-Host "Errore: sei nel branch '$BRANCH', devi essere nel branch '$EXPECTED_BRANCH' per fermare l'ambiente $ENV."
  exit 1
}

docker compose -f $COMPOSE_FILE down