

## Panoramica Progetto

Campusly è una piattaforma social universitaria. Questo è il backend REST API costruito con **Spring Boot 4.0.5** e **Java 21**.  
L'app gestisce autenticazione (locale + Google OAuth2), profili utente, università e un sistema di "spotted" (post anonimi/non).

---

## Stack Tecnologico

| Componente        | Tecnologia                              |
| ----------------- | --------------------------------------- |
| Framework         | Spring Boot **4.0.5**                   |
| Linguaggio        | Java **21**                             |
| Build tool        | Maven (wrapper `mvnw` / `mvnw.cmd`)     |
| Database          | PostgreSQL **16** (Alpine)              |
| ORM               | Spring Data JPA + Hibernate             |
| Migrazioni DB     | Liquibase (formatted SQL)               |
| Autenticazione    | JWT (jjwt **0.13.0**) + OAuth2 (Google) |
| Password hashing  | BCrypt                                  |
| Validazione       | Jakarta Validation (`@Valid`)           |
| Mapping DTO       | MapStruct **1.6.2**                     |
| API Docs          | SpringDoc OpenAPI **3.0.2** (Swagger)   |
| WebSocket         | Spring WebSocket                        |
| Monitoring        | Spring Actuator                         |
| Lombok            | Sì (Getter/Setter/Builder/etc.)         |
| Test              | JUnit 5, Spring Security Test, H2       |
| Container         | Docker multi-stage (Maven + Temurin 23) |
| Reverse Proxy     | Traefik (produzione)                    |

---

## Struttura del Progetto

```
src/main/java/com/campusly/campusly_backend/
├── CampuslyBackendApplication.java          # Entry point (@EnableScheduling)
├── auth/                                     # Modulo autenticazione
│   ├── controller/AuthController.java        # REST endpoints /api/auth/*
│   ├── dto/                                  # Record DTO (immutabili)
│   │   ├── BirthDate.java                    # Record { day, month, year } con toLocalDate()
│   │   ├── LoginRequest.java
│   │   ├── RegisterUserRequest.java          # Registrazione utente (CAMPUSLY_USER)
│   │   ├── RegisterCreatorRequest.java       # Registrazione creator (CAMPUSLY_CREATOR)
│   │   ├── UserAuthResponse.java
│   │   └── UserProfileResponse.java
│   ├── entity/                               # Entità JPA
│   │   ├── User.java
│   │   ├── Role.java                         # enum: CAMPUSLY_USER, CAMPUSLY_CREATOR, CAMPUSLY_MODERATOR, SUPER_ADMIN
│   │   └── AuthProvider.java                 # enum: LOCAL, GOOGLE
│   ├── repository/UserRepository.java
│   └── service/AuthService.java
└── shared/                                   # Componenti trasversali
    ├── config/
    │   ├── SecurityConfig.java               # Security filter chain, CORS, BCrypt
    │   └── CorsConfig.java                   # (vuoto, CORS in SecurityConfig)
    ├── validation/                           # Validazioni custom
    │   ├── ValidPhoneNumber.java             # Annotation per formato telefono (+XX-XXXXXXX)
    │   └── PhoneNumberValidator.java         # ConstraintValidator implementazione
    ├── exception/                            # Gestione errori centralizzata
    │   ├── CustomResponse.java               # Wrapper risposta standard
    │   ├── ErrorDetail.java                  # record(title, message, payload)
    │   ├── ExceptionBackend.java             # Eccezione custom con HttpStatus
    │   ├── ExceptionUtilService.java         # Handler nel controller
    │   └── GlobalExceptionHandler.java       # @RestControllerAdvice globale
    └── security/                             # Infrastruttura JWT e OAuth2
        ├── JwtService.java                   # Generazione/validazione JWT
        ├── JwtAuthenticationFilter.java      # OncePerRequestFilter (cookie-based)
        ├── JwtAuthenticationEntryPoint.java  # 401 JSON response
        ├── JwtAccessDeniedHandler.java       # 403 JSON response
        ├── OAuth2AuthenticationSuccessHandler.java
        └── TokenBlacklistService.java        # Blacklist in-memory con cleanup
```

---

## Convenzioni di Codice

### Generali
- **Lingua dei messaggi utente**: italiano (messaggi di errore, validazione, log info).
- **Lingua del codice**: inglese per nomi di classi, metodi e variabili.
- **Commenti/Javadoc**: italiano.
- **Package base**: `com.campusly.campusly_backend`.
- **Moduli**: organizzati per dominio (`auth`, `shared`). Ogni modulo ha sotto-package `controller`, `dto`, `entity`, `repository`, `service`.

### Entità JPA
- UUID come primary key (`@GeneratedValue(strategy = GenerationType.UUID)`).
- Nomi colonne in **snake_case inglese** (es. `first_name`, `last_name`, `birth_date`).
- Uso di `@Builder` + `@Builder.Default` per valori default.
- `@CreationTimestamp` per `created_at` (tipo `LocalDateTime`).
- `ddl-auto: validate` — lo schema è gestito esclusivamente da Liquibase.

### DTO
- Usare **Java Record** per tutti i DTO (immutabili).
- Validazione con Jakarta Validation annotations direttamente sul record.
- Validazioni custom nel package `shared/validation/` (es. `@ValidPhoneNumber`).
- Username è fornito dall'utente in registrazione (validato con regex `^[a-zA-Z0-9._]+$`).
- `birthDate` nei DTO di registrazione è un oggetto `BirthDate { day, month, year }` (tutti `Long`), non un `LocalDate`. La conversione a `LocalDate` avviene nel service tramite `toLocalDate()`.

### Controller
- Prefisso API: `/api/<modulo>/...` (es. `/api/auth/register`).
- Ogni metodo controller wrappa la risposta in `CustomResponse`.
- Pattern try/catch nel controller con delega a `ExceptionUtilService.handleAnyException()`.
- Iniettare `HttpServletRequest` e `HttpServletResponse` come parametri del metodo.
- Annotazione `@Valid` su `@RequestBody`.

### Service
- Annotazione `@Service` + `@RequiredArgsConstructor`.
- `@Transactional` per operazioni di scrittura, `@Transactional(readOnly = true)` per lettura.
- Log con `@Slf4j`.
- Lanciare `ExceptionBackend.fromError(title, message, payload, HttpStatus)` per errori.
- Lanciare `ExceptionBackend.fromWarning(...)` per warning non bloccanti.
- Codici errore nel formato `"CODICE: XX000"` appeso al messaggio.

### Repository
- Estendere `JpaRepository<Entity, UUID>`.
- Metodi di query derivati (findByEmail, existsByUsername, ecc.).

---

## Sistema di Gestione Errori

Il progetto usa un pattern a due livelli:

### 1. `ExceptionBackend` (eccezione custom)
```java
ExceptionBackend.fromError("Titolo", "Messaggio, CODICE: XX000", payloadObj, HttpStatus.CONFLICT);
ExceptionBackend.fromWarning("Titolo", "Messaggio", payloadObj, HttpStatus.OK);
```

### 2. `CustomResponse` (wrapper risposta uniforme)
Tutte le risposte API seguono questa struttura:
```json
{
  "data": { ... },
  "method": "POST",
  "error": { "title": "...", "message": "...", "payload": ... },
  "warning": { "title": "...", "message": "...", "payload": ... }
}
```
- `data`: presente in caso di successo.
- `error` / `warning`: presente in caso di errore/warning (mutuamente esclusivi di norma).
- `method`: metodo HTTP della richiesta.

### 3. `GlobalExceptionHandler`
- Gestisce `ExceptionBackend`, `MethodArgumentNotValidException`, `NoHandlerFoundException`, e fallback generico.
- Su 401, cancella il cookie `token`.

### Codici errore attivi
| Codice | Contesto            | Significato                                |
| ------ | ------------------- | ------------------------------------------ |
| AU100  | Registrazione       | Email già registrata                       |
| AU101  | Registrazione       | Utente minore di 16 anni                   |
| AU102  | Registrazione       | Username già in uso                        |
| AU200  | Login               | Credenziali non valide (utente non esiste) |
| AU201  | Login               | Provider sbagliato (es. LOCAL vs GOOGLE)   |
| AU202  | Login               | Password errata                            |
| AU300  | Profilo (/me)       | Utente non trovato                         |

---

## Autenticazione e Sicurezza

### JWT (Cookie-based)
- Il token JWT è memorizzato in un **cookie HttpOnly** chiamato `token`.
- **NON** usa header `Authorization: Bearer`. Il filtro legge il token dal cookie.
- Claims del JWT: `sub` (email), `userId` (UUID string), `role`.
- Scadenza configurabile via `app.security.jwt.access-token-expiration-ms` (default 15 min).
- Cookie attributes: `HttpOnly=true`, `Secure` dipende da env `SECURE`, `SameSite=None|Lax`.

### Token Blacklist (Logout)
- `TokenBlacklistService` mantiene una `ConcurrentHashMap<String, Date>` in memoria.
- Cleanup dei token scaduti ogni 15 minuti (`@Scheduled`).
- **Nota**: in produzione multi-istanza, sostituire con Redis.

### OAuth2 (Google)
- Flusso standard Spring OAuth2 Login.
- `OAuth2AuthenticationSuccessHandler` genera il JWT e lo setta come cookie.
- Redirect configurabile verso il frontend via `app.security.oauth2.frontend-redirect-url`.

### Security Filter Chain
- Session policy: `STATELESS`.
- CSRF: disabilitato.
- Endpoint pubblici: `/api/auth/login`, `/api/auth/register`, `/api/auth/register/creator`, `/api-docs/**`, `/actuator/**`, `/oauth2/**`, `/login/oauth2/**`.
- Tutti gli altri endpoint richiedono autenticazione.
- CORS: permette `http://localhost:4200` (Angular frontend).

### Password
- Hashing con `BCryptPasswordEncoder`.

---

## Database

### PostgreSQL
- Target: PostgreSQL 16 (Alpine).
- Database: `campusly_db`, User: `campusly_user` (locale).
- Produzione: credenziali via variabili d'ambiente.

### Liquibase (Migrazioni)
- File master: `src/main/resources/db/changelog/db-changelog-master.xml`.
- Migrazioni in `db/changelog/migrations/` numerate (`001-init.sql`, `002-...`).
- Formato: `-- liquibase formatted sql` + `-- changeset campusly:<nome-file>`.
- Hibernate `ddl-auto: validate` — non genera/modifica schema, solo validazione.

### Schema attuale

**Tabella `universities`:**
| Colonna          | Tipo         | Note           |
| ---------------- | ------------ | -------------- |
| id               | UUID (PK)    | auto-generated |
| name             | VARCHAR(255) | NOT NULL       |
| country          | VARCHAR(100) | NOT NULL       |
| city             | VARCHAR(100) | NOT NULL       |
| email_domain     | VARCHAR(255) | UNIQUE         |
| international    | BOOLEAN      | default false  |
| created_at       | TIMESTAMP    | default now    |

**Tabella `users`:**
| Colonna          | Tipo         | Note                       |
| ---------------- | ------------ | -------------------------- |
| id               | UUID (PK)    | auto-generated             |
| username         | VARCHAR(50)  | UNIQUE, NOT NULL           |
| first_name       | VARCHAR(100) | NOT NULL                   |
| last_name        | VARCHAR(100) | NOT NULL                   |
| email            | VARCHAR(255) | UNIQUE, NOT NULL           |
| password_hash    | VARCHAR(255) |                            |
| birth_date       | DATE         |                            |
| phone            | VARCHAR(25)  |                            |
| university_id    | UUID (FK)    | → universities(id)         |
| erasmus_univ_id  | UUID (FK)    | → universities(id)         |
| is_erasmus       | BOOLEAN      | default false              |
| photo_url        | TEXT         |                            |
| bio              | VARCHAR(300) |                            |
| auth_provider    | VARCHAR(20)  | default 'LOCAL'            |
| role             | VARCHAR(20)  | default 'CAMPUSLY_USER'    |
| created_at       | TIMESTAMP    | default now                |

**Tabella `spotted`:**
| Colonna          | Tipo         | Note                       |
| ---------------- | ------------ | -------------------------- |
| id               | UUID (PK)    | auto-generated             |
| author_id        | UUID (FK)    | → users(id)                |
| content          | TEXT         | NOT NULL                   |
| category         | VARCHAR(30)  | NOT NULL                   |
| university_id    | UUID (FK)    | → universities(id), NOT NULL |
| is_anonymous     | BOOLEAN      | default false              |
| like_count       | INTEGER      | default 0                  |
| comment_count    | INTEGER      | default 0                  |
| status           | VARCHAR(20)  | default 'ACTIVE'           |
| created_at       | TIMESTAMP    | default now                |

Indici: `idx_spotted_university`, `idx_spotted_created_at DESC`.

**Nota:** Le tabelle `universities` e `spotted` esistono nel DB (via Liquibase) ma **non hanno ancora entità JPA, repository o service** — sono da implementare.

---

## API Endpoints

### Auth (`/api/auth`)

| Metodo | Endpoint                      | Auth  | Descrizione                         | Body                    | Risposta                  |
| ------ | ----------------------------- | ----- | ----------------------------------- | ----------------------- | ------------------------- |
| POST   | `/api/auth/register`          | No    | Registra utente (CAMPUSLY_USER)     | `RegisterUserRequest`   | 201 + `UserAuthResponse`  |
| POST   | `/api/auth/register/creator`  | No    | Registra creator (CAMPUSLY_CREATOR) | `RegisterCreatorRequest` | 201 + `UserAuthResponse` |
| POST   | `/api/auth/login`             | No    | Login con email/password            | `LoginRequest`          | 200 + `UserAuthResponse`  |
| GET    | `/api/auth/me`                | Sì    | Profilo utente autenticato          | —                       | 200 + `UserProfileResponse` |
| POST   | `/api/auth/logout`            | Sì    | Logout (invalida token)             | —                       | 200                       |

---

## Profili Spring

| Profilo | Uso                | Config                    | Note                                    |
| ------- | ------------------ | ------------------------- | --------------------------------------- |
| `local` | Sviluppo locale    | `application-local.yml`   | DB su localhost:5432, log DEBUG          |
| `prod`  | Produzione (VPS)   | `application-prod.yml`    | Tutto via env vars, log ridotto          |

---

## Docker

### Sviluppo locale
```bash
docker compose -f docker-compose-local.yml up --build
```
- Richiede file `.env.local`.
- Porta DB esposta (5432), porta app esposta (8080).

### Produzione
```bash
docker compose up -d
```
- Richiede file `.env`.
- Traefik come reverse proxy con TLS Let's Encrypt.
- Nessuna porta esposta direttamente.

### Dockerfile
- Multi-stage: Maven build → JRE Alpine runtime.
- Immagine base: `eclipse-temurin:23`.
- Porta esposta: 8080.

---

## Variabili d'Ambiente Richieste

| Variabile                    | Descrizione                                 |
| ---------------------------- | ------------------------------------------- |
| `JWT_SECRET`                 | Chiave segreta per firma JWT (Base64)        |
| `JWT_ACCESS_EXPIRATION`      | Scadenza access token in ms (default 900000) |
| `GOOGLE_CLIENT_ID`           | Client ID OAuth2 Google                      |
| `GOOGLE_CLIENT_SECRET`       | Client Secret OAuth2 Google                  |
| `OAUTH2_FRONTEND_REDIRECT`   | URL redirect post-login OAuth2 verso frontend|
| `SECURE`                     | "true" per cookie Secure + SameSite=None     |
| `SPRING_DATASOURCE_URL`      | JDBC URL PostgreSQL (prod)                   |
| `SPRING_DATASOURCE_USERNAME` | Username DB (prod)                           |
| `SPRING_DATASOURCE_PASSWORD` | Password DB (prod)                           |
| `POSTGRES_DB`                | Nome database (Docker)                       |
| `POSTGRES_USER`              | Utente database (Docker)                     |
| `POSTGRES_PASSWORD`          | Password database (Docker)                   |

---

## Regole per Generare Nuovo Codice

### Quando crei una nuova entità:
1. Creare entità JPA in `<modulo>/entity/` con UUID PK, `@Builder`, Lombok.
2. Nomi colonne in snake_case inglese.
3. Creare migration Liquibase in `db/changelog/migrations/` con numerazione sequenziale.
4. Aggiungere `<include>` in `db-changelog-master.xml`.
5. Creare Repository in `<modulo>/repository/` che estende `JpaRepository<Entity, UUID>`.

### Quando crei un nuovo endpoint:
1. Creare DTO come Java Record con validazioni Jakarta.
2. Creare metodo nel Service con `@Transactional`.
3. Creare metodo nel Controller con pattern try/catch + `CustomResponse`.
4. Usare `ExceptionBackend.fromError()` per errori di business.
5. Codici errore nel formato `XX000` con prefisso domain-specific.
6. **OBBLIGATORIO:** Al termine, fornire all'utente:
   - L'**URL completo di test** (es. `http://localhost:8080/api/...`)
   - Il **body JSON** di esempio per richieste POST/PUT/PATCH
   - Un **esempio di response** di successo (con struttura `CustomResponse`)
   - Un **esempio di response** di errore (se applicabile)

   Formato da usare:
   ```
   URL: POST http://localhost:8080/api/<modulo>/<endpoint>

   Body:
   { ... }

   Response (successo):
   { "data": { ... }, "method": "POST", "error": null, "warning": null }

   Response (errore):
   { "data": null, "method": "POST", "error": { "title": "...", "message": "...", "payload": ... }, "warning": null }
   ```

### Quando crei una nuova migrazione:
```sql
-- liquibase formatted sql
-- changeset campusly:<NNN-descrizione>

-- title: <Titolo descrittivo della migrazione> --
-- tabelle modificate: <tab1, tab2 oppure vuoto se nessuna> --
-- tabelle create: <tab1, tab2 oppure vuoto se nessuna> --

-- SQL qui
```

### Convenzioni di naming:
- Package: `com.campusly.campusly_backend.<modulo>.<layer>`
- Controller: `<Modulo>Controller` → `@RequestMapping("/api/<modulo>")`
- Service: `<Modulo>Service`
- Repository: `<Entity>Repository`
- DTO: `<Azione>Request`, `<Entity><Azione>Response`

---

## Cose da Sapere / Gotcha

1. **Il frontend è Angular V19** su `http://localhost:4200` — CORS configurato per questo.
2. **JWT via cookie**, non via header Authorization — tutte le chiamate devono avere `credentials: 'include'` lato frontend.
3. **Token blacklist in-memory** — non persiste tra restart, non distribuita.
4. **Tabelle `universities` e `spotted` nel DB ma senza codice Java** — sono il prossimo passo di sviluppo.
5. **Hibernate in modalità validate** — mai toccare `ddl-auto`, ogni cambio schema va fatto con Liquibase.
6. **MapStruct** è configurato come dipendenza ma non ancora usato — da usare per il mapping entity↔DTO dove serve.
7. **WebSocket** è nel POM ma non ancora implementato — previsto per chat/notifiche real-time.
8. **`CorsConfig.java` è vuoto** — la config CORS reale è nel metodo `corsConfigurationSource()` dentro `SecurityConfig`.
9. **Sistema di ruoli a 4 livelli**: `CAMPUSLY_USER` (default), `CAMPUSLY_CREATOR`, `CAMPUSLY_MODERATOR`, `SUPER_ADMIN`. La registrazione standard crea `CAMPUSLY_USER`, l'endpoint `/register/creator` crea `CAMPUSLY_CREATOR`.
10. **Username fornito dall'utente** in registrazione (validato `^[a-zA-Z0-9._]+$`), non più auto-generato.
11. **OAuth2 login** crea utente con ruolo `CAMPUSLY_USER` e `userId` deterministico da `UUID.nameUUIDFromBytes(email.getBytes())` — potenzialmente da cambiare se si vuole persistere l'utente OAuth2 nel DB.
12. **Validazione telefono** custom con `@ValidPhoneNumber` — formato `+{1-4 cifre}-{6-15 cifre}` (es. `+39-3331234567`).
13. **Età minima 16 anni** per la registrazione, validata lato service.

---

## Aggiornamento della Skill

**REGOLA OBBLIGATORIA:** Ogni volta che viene implementata una nuova funzionalità, modificata l'architettura, aggiunto un endpoint, creata una nuova entità, o effettuato qualsiasi cambiamento significativo al progetto, **devi chiedere all'utente** se vuole aggiornare questo file SKILL.md per riflettere le modifiche.

Esempio di domanda da porre:
> "Ho completato l'implementazione. Vuoi che aggiorni il file SKILL.md con le nuove modifiche?"

Non aggiornare mai questo file autonomamente senza conferma esplicita dell'utente.
