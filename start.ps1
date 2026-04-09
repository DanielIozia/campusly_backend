param([string]$ENV)

$BRANCH = git rev-parse --abbrev-ref HEAD

switch ($ENV) {
  "local" { $EXPECTED_BRANCH = "local"; $COMPOSE_FILE = "docker-compose-local.yml"; $ENV_FILE = ".env.local" }
  "dev"   { $EXPECTED_BRANCH = "dev";   $COMPOSE_FILE = "docker-compose-dev.yml";   $ENV_FILE = ".env.dev"   }
  "prod"  { $EXPECTED_BRANCH = "main";  $COMPOSE_FILE = "docker-compose.yml";       $ENV_FILE = ".env"       }
  default { Write-Host "Uso: .\start.ps1 [local|dev|prod]"; exit 1 }
}

if ($BRANCH -ne $EXPECTED_BRANCH) {
  Write-Host "Errore: sei nel branch '$BRANCH', devi essere nel branch '$EXPECTED_BRANCH' per avviare l'ambiente $ENV."
  exit 1
}

docker compose -f $COMPOSE_FILE --env-file $ENV_FILE up --build