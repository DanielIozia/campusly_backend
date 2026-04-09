# Campusly Backend

Backend REST API per la piattaforma Campusly, costruito con **Spring Boot 4** e **Java 21**.

## Tech Stack

| Tecnologia | Versione | Scopo |
|---|---|---|
| Java | 21 | Linguaggio |
| Spring Boot | 4.0.5 | Framework |
| Spring Security | — | Autenticazione e autorizzazione |
| PostgreSQL | 16 | Database |
| Liquibase | — | Migrazioni DB |
| jjwt | 0.13.0 | Token JWT (HttpOnly cookie) |
| Lombok | — | Riduzione boilerplate |
| MapStruct | — | Mapping DTO ↔ Entity |
| SpringDoc OpenAPI | 3.0.2 | Documentazione API (Swagger) |
| Docker | — | Containerizzazione |

## Struttura progetto

```
src/main/java/com/campusly/campusly_backend/
├── auth/
│   ├── controller/     # AuthController (login, register, logout, me)
│   ├── dto/            # Request/Response DTO
│   ├── entity/         # User, Role, AuthProvider
│   ├── repository/     # UserRepository
│   └── service/        # AuthService
└── shared/
    ├── config/         # SecurityConfig, CorsConfig
    ├── exception/      # GlobalExceptionHandler, CustomResponse
    └── security/       # JwtService, JwtAuthenticationFilter, OAuth2Handler
```

## Prerequisiti

- **Docker Desktop** installato e avviato
- File **`.env.local`** nella root del progetto (con le variabili necessarie per il database e l'applicazione)


## Ambienti e avvio

Il progetto supporta 3 ambienti separati, ognuno con il proprio branch Git e database Neon dedicato.

| Ambiente | Branch | Database |
|----------|--------|----------|
| Local    | `local` | Neon branch `local` |
| Dev      | `dev`   | Neon branch `dev`   |
| Prod     | `prod`  | Neon branch `production`  |

> Prima di avviare, assicurati di essere nel branch corretto e di avere il file `.env` relativo all'ambiente nella root del progetto. Puoi usare `.env.example` come riferimento.

### Avvio

```sh
# Local
git checkout local
./start.ps1 local        # linux/mac: .\start.sh local

# Dev
git checkout dev
./start.ps1 dev          # linux/mac: .\start.sh dev
```

### Stop

```sh
./stop.ps1 local         # linux/mac: .\stop.sh local
./stop.ps1 dev            # linux/mac: .\stop.sh dev
```

## API Endpoints

### Autenticazione

| Metodo | Endpoint | Accesso | Descrizione |
|---|---|---|---|
| `POST` | `/api/auth/register` | Pubblico | Registrazione nuovo utente |
| `POST` | `/api/auth/login` | Pubblico | Login con email/password |
| `POST` | `/api/auth/logout` | Autenticato | Logout (invalida token + cancella cookie) |
| `GET` | `/api/auth/me` | Autenticato | Profilo utente autenticato |

### OAuth2

| Metodo | Endpoint | Descrizione |
|---|---|---|
| `GET` | `/oauth2/authorization/google` | Avvia login con Google |

### Documentazione & Monitoraggio

| Endpoint | Descrizione |
|---|---|
| `/api-docs` | OpenAPI JSON |
| `/swagger-ui.html` | Swagger UI |
| `/actuator/health` | Health check |

## Autenticazione

Il backend usa un **singolo token JWT** salvato in un **cookie HttpOnly**:

- Al **login/register**, il server setta un cookie `token` nella risposta
- Il browser lo invia automaticamente ad ogni richiesta
- JavaScript **non può** leggere il cookie (HttpOnly)
- Al **logout**, il cookie viene cancellato e il token aggiunto alla blacklist

Per i dettagli tecnici sulla configurazione Spring Security e CORS, vedi [SECURITY.md](SECURITY.md).

## Configurazione database locale

Ogni membro del team ha un proprio database locale separato su Neon.

### Step 1 — Crea un account su Neon
Vai su [neon.tech](https://neon.tech) e registrati.

### Step 2 — Crea un progetto
1. Crea un nuovo progetto e chiamalo `campusly-local`
2. Scegli la region `eu-central-1` (Frankfurt)
3. Dalla dashboard vai su **Connection string**, seleziona il formato **JDBC** e copia la stringa

### Step 3 — Crea il file `.env.local`
Nella root del progetto crea il file `.env.local` (non viene committato) con le credenziali appena ottenute da Neon:

```env
SPRING_DATASOURCE_URL=jdbc:postgresql://<host>.neon.tech/<dbname>?sslmode=require
SPRING_DATASOURCE_USERNAME=<user>
SPRING_DATASOURCE_PASSWORD=<password>

APP_PORT=8080
JWT_SECRET=<genera con: openssl rand -hex 64>
GOOGLE_CLIENT_ID=<chiedi al team>
GOOGLE_CLIENT_SECRET=<chiedi al team>
OAUTH2_FRONTEND_REDIRECT=http://localhost:4200/oauth2/callback
```

> `GOOGLE_CLIENT_ID` e `GOOGLE_CLIENT_SECRET` sono condivisi dal team — chiedili a chi gestisce il progetto.

### Step 4 — Avvia il backend
```powershell
git checkout local
.\start.ps1 local
```

Liquibase creerà automaticamente tutte le tabelle al primo avvio.

## Migrazioni Database

Le migrazioni sono gestite da **Liquibase** e si trovano in:

```
src/main/resources/db/changelog/migrations/
```

Vengono applicate automaticamente all'avvio dell'applicazione. Per aggiungere una nuova migrazione, crea un file SQL nella cartella e registralo in `db-changelog-master.xml`.
