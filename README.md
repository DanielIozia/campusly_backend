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
- File **`.env.local`** nella root del progetto (vedi sezione sotto)

## Variabili d'ambiente

Crea un file `.env.local` nella root del progetto:

```env
# Database
POSTGRES_DB=campusly_db
POSTGRES_USER=campusly_user
POSTGRES_PASSWORD=campusly_pass
POSTGRES_PORT=5432

# App
APP_PORT=8080
SECURE=false

# JWT
JWT_SECRET=<chiave base64 di almeno 256 bit>
JWT_ACCESS_EXPIRATION=900000

# OAuth2 Google (opzionale in dev)
GOOGLE_CLIENT_ID=placeholder
GOOGLE_CLIENT_SECRET=placeholder
OAUTH2_FRONTEND_REDIRECT=http://localhost:4200/oauth2/callback
```

## Avvio con Docker

**Avviare (build + start):**
```bash
docker compose -f docker-compose-local.yml --env-file .env.local up --build
```

**Fermare (mantiene i dati nel DB):**
```bash
docker compose -f docker-compose-local.yml --env-file .env.local down
```

**Fermare e cancellare i dati del DB:**
```bash
docker compose -f docker-compose-local.yml --env-file .env.local down -v
```

> Il flag `-v` rimuove i volumi Docker, quindi **cancella tutti i dati del database**. Usalo solo quando vuoi resettare il DB.

## Avvio senza Docker (sviluppo locale)

Richiede un'istanza PostgreSQL in esecuzione su `localhost:5432`.

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=local
```

Oppure su Windows:
```powershell
.\mvnw.cmd spring-boot:run -D"spring-boot.run.profiles=local"
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

## Migrazioni Database

Le migrazioni sono gestite da **Liquibase** e si trovano in:

```
src/main/resources/db/changelog/migrations/
```

Vengono applicate automaticamente all'avvio dell'applicazione. Per aggiungere una nuova migrazione, crea un file SQL nella cartella e registralo in `db-changelog-master.xml`.
