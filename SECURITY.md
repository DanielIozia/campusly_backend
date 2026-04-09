# Spring Security & CORS — Come funziona nel progetto Campusly

## Indice

1. [Panoramica](#1-panoramica)
2. [SecurityConfig — Metodo per metodo](#2-securityconfig--metodo-per-metodo)
3. [CORS — Cos'è e come funziona](#3-cors--cosè-e-come-funziona)
4. [JwtAuthenticationFilter — Il filtro JWT](#4-jwtauthenticationfilter--il-filtro-jwt)
5. [JwtAuthenticationEntryPoint — Errore 401](#5-jwtauthenticationentrypoint--errore-401)
6. [JwtAccessDeniedHandler — Errore 403](#6-jwtaccessdeniedhandler--errore-403)
7. [Flusso completo di una richiesta](#7-flusso-completo-di-una-richiesta)

---

## 1. Panoramica

Il sistema di sicurezza è **stateless** (senza sessioni server-side). L'autenticazione avviene tramite un **JWT salvato in un cookie HttpOnly**. Ogni richiesta viene intercettata da una catena di filtri (filter chain) che verifica il token e decide se l'utente è autenticato.

---

## 2. SecurityConfig — Metodo per metodo

### `securityFilterChain(HttpSecurity http)`

Questo è il metodo centrale. Configura **tutta** la pipeline di sicurezza. Ogni riga (metodo) nella catena fa qualcosa di specifico:

---

#### `.cors(cors -> cors.configurationSource(corsConfigurationSource()))`

**Cosa fa:** Abilita il supporto CORS (Cross-Origin Resource Sharing) dentro Spring Security e gli dice di usare la configurazione definita nel metodo `corsConfigurationSource()`.

**Perché serve:** Senza questa riga, Spring Security bloccherebbe le richieste preflight OPTIONS prima ancora che il CORS venga valutato. Mettendolo qui, Spring Security sa che deve lasciar passare le richieste OPTIONS del browser e aggiungere gli header CORS alle risposte.

---

#### `.csrf(csrf -> csrf.disable())`

**Cosa fa:** Disabilita la protezione CSRF (Cross-Site Request Forgery).

**Perché serve:** La protezione CSRF è pensata per applicazioni con sessioni e cookie di sessione classici. Nel nostro caso usiamo JWT stateless — ogni richiesta è indipendente. Inoltre il nostro cookie JWT è HttpOnly, quindi il browser lo invia automaticamente ma JavaScript non può leggerlo, riducendo il rischio di attacchi CSRF. In un'API REST stateless, CSRF si disabilita.

---

#### `.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`

**Cosa fa:** Dice a Spring Security di **non creare mai sessioni HTTP** (niente `JSESSIONID`).

**Perché serve:** Il nostro backend è completamente stateless. L'autenticazione è contenuta interamente nel JWT dentro al cookie. Il server non mantiene nessuno stato in memoria tra una richiesta e l'altra. Questo rende il backend facilmente scalabile (puoi mettere N istanze dietro un load balancer senza problemi di sessione condivisa).

---

#### `.exceptionHandling(exceptions -> exceptions ...)`

```java
.authenticationEntryPoint(authenticationEntryPoint)
.accessDeniedHandler(accessDeniedHandler)
```

**Cosa fa:** Configura come rispondere quando:
- **`authenticationEntryPoint`** → L'utente **non è autenticato** (manca il token o è invalido) → risponde **401 Unauthorized**
- **`accessDeniedHandler`** → L'utente **è autenticato** ma **non ha i permessi** per quella risorsa → risponde **403 Forbidden**

**Perché serve:** Senza questi handler, Spring Security restituirebbe una pagina HTML di errore di default. Con i nostri handler custom, restituiamo sempre un JSON coerente con il formato `CustomResponse` usato in tutto il progetto.

---

#### `.authorizeHttpRequests(auth -> auth ...)`

```java
.requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
.requestMatchers("/api-docs/**").permitAll()
.requestMatchers("/actuator/**").permitAll()
.requestMatchers("/oauth2/**", "/login/oauth2/**").permitAll()
.anyRequest().authenticated()
```

**Cosa fa:** Definisce le **regole di accesso** per ogni URL:

| Pattern | Accesso | Motivo |
|---------|---------|--------|
| `/api/auth/login`, `/api/auth/register` | Pubblico | L'utente deve poter fare login/register senza essere già autenticato |
| `/api-docs/**` | Pubblico | Documentazione Swagger/OpenAPI accessibile senza token |
| `/actuator/**` | Pubblico | Endpoint di monitoraggio (health check, ecc.) |
| `/oauth2/**`, `/login/oauth2/**` | Pubblico | Flusso OAuth2 (redirect a Google, callback) |
| **Tutto il resto** | Autenticato | Qualsiasi altro endpoint richiede un JWT valido |

**`permitAll()`** = chiunque può accedere, anche senza token.  
**`authenticated()`** = serve un token JWT valido nel cookie.

---

#### `.oauth2Login(oauth2 -> oauth2.successHandler(oAuth2SuccessHandler))`

**Cosa fa:** Abilita il login OAuth2 (Google) e configura cosa fare **dopo** che Google ha autenticato l'utente con successo.

**Perché serve:** Quando l'utente si logga con Google, Spring Security gestisce tutto il flusso OAuth2 (redirect a Google, ricezione del codice, scambio del codice per i dati utente). Dopo il successo, chiama il nostro `oAuth2SuccessHandler` che genera il JWT, lo mette in un cookie HttpOnly e redirige l'utente al frontend.

---

#### `.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`

**Cosa fa:** Inserisce il nostro `JwtAuthenticationFilter` **prima** del filtro standard di Spring Security (`UsernamePasswordAuthenticationFilter`).

**Perché serve:** In questo modo, ad ogni richiesta, il nostro filtro viene eseguito per primo. Legge il cookie JWT, lo valida, e se è valido popola il `SecurityContext` con i dati dell'utente. Quando Spring Security arriva a verificare se la richiesta è "authenticated", trova già l'utente nel contesto e la lascia passare.

**Ordine dei filtri nella catena:**
```
Richiesta HTTP
  → CorsFilter (gestisce OPTIONS preflight)
  → JwtAuthenticationFilter (legge cookie, valida JWT, setta SecurityContext)
  → UsernamePasswordAuthenticationFilter (Spring lo skippa perché l'auth è già settata)
  → AuthorizationFilter (verifica permitAll/authenticated)
  → Controller
```

---

### `passwordEncoder()`

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```

**Cosa fa:** Crea un bean per l'hashing delle password con l'algoritmo **BCrypt**.

**Perché serve:** Le password non vengono mai salvate in chiaro nel database. Quando un utente si registra, la password viene hashata con BCrypt prima di essere salvata. Al login, BCrypt confronta la password inserita con l'hash salvato.

**BCrypt** è un algoritmo deliberatamente lento (con fattore di costo configurabile), resistente a brute-force e rainbow table attacks.

---

### `corsConfigurationSource()`

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("http://localhost:4200"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}
```

Ogni riga spiegata nella sezione CORS qui sotto.

---

## 3. CORS — Cos'è e come funziona

### Il problema

Il browser ha una regola di sicurezza chiamata **Same-Origin Policy**: JavaScript in una pagina può fare richieste HTTP solo allo **stesso dominio** da cui la pagina è stata caricata.

Nel nostro caso:
- Frontend Angular gira su `http://localhost:4200`
- Backend Spring Boot gira su `http://localhost:8080`

Sono **origini diverse** (porta diversa = origine diversa). Senza CORS, il browser bloccherebbe tutte le richieste dal frontend al backend.

### Come CORS risolve il problema

CORS (Cross-Origin Resource Sharing) è un meccanismo dove il **server** dice al browser: "Accetto richieste da questa origine". Lo fa tramite header HTTP nella risposta.

### Le richieste preflight (OPTIONS)

Per richieste "complesse" (POST con JSON, richieste con cookie, ecc.), il browser prima invia una **richiesta preflight** — una richiesta OPTIONS automatica per chiedere al server: "Posso mandarti una POST da questa origine?". Il server risponde con gli header CORS. Solo se la risposta è OK, il browser invia la richiesta vera.

```
Browser                          Server
  |                                |
  |--- OPTIONS /api/auth/login --->|  (preflight: "posso fare POST da localhost:4200?")
  |<-- 200 OK + header CORS ------|  (risposta: "sì, accetto")
  |--- POST /api/auth/login ------>|  (richiesta vera)
  |<-- 200 OK + dati -------------|
```

### Configurazione metodo per metodo

#### `setAllowedOrigins(List.of("http://localhost:4200"))`
Lista dei domini che possono fare richieste al backend. Solo `http://localhost:4200` è autorizzato. Qualsiasi altro dominio viene bloccato.

#### `setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"))`
Metodi HTTP permessi. OPTIONS è necessario per le preflight. Gli altri sono i metodi usati dall'API REST.

#### `setAllowedHeaders(List.of("*"))`
Accetta qualsiasi header nella richiesta (Content-Type, Accept, ecc.). Il `*` è un wildcard che significa "tutti".

#### `setAllowCredentials(true)`
**Fondamentale per i cookie.** Dice al browser: "Puoi inviare i cookie con le richieste cross-origin". Senza questo, il browser non invierebbe il cookie `token` al backend e l'utente risulterebbe sempre non autenticato.

> **Nota:** Quando `allowCredentials` è `true`, non puoi usare `*` come origine — devi specificare l'URL esatto. Questo è un vincolo di sicurezza del browser.

#### `registerCorsConfiguration("/**", config)`
Applica la configurazione CORS a **tutti** i path (`/**`). Il pattern `/**` significa "qualsiasi URL, inclusi sotto-path".

---

## 4. JwtAuthenticationFilter — Il filtro JWT

Questo filtro viene eseguito ad **ogni richiesta HTTP**. Il suo lavoro è:

1. **Leggere il cookie** `token` dalla richiesta (`jwtService.getTokenFromCookie(request)`)
2. **Se non c'è il cookie** → lascia passare la richiesta senza autenticazione (sarà il `authorizeHttpRequests` a decidere se è permessa o no)
3. **Se il token è in blacklist** (logout) → lascia passare senza autenticazione
4. **Se il token è valido** → estrae email e ruolo dal JWT, crea un oggetto `Authentication` e lo mette nel `SecurityContextHolder`

### Il SecurityContext

Il `SecurityContextHolder` è il "contenitore" dove Spring Security tiene traccia di chi è l'utente autenticato per la richiesta corrente. Quando il filtro ci mette dentro un `UsernamePasswordAuthenticationToken`, Spring Security considera la richiesta come autenticata.

```java
UsernamePasswordAuthenticationToken authentication =
    new UsernamePasswordAuthenticationToken(email, null, authorities);
```

- **`email`** → il principal (chi è l'utente)
- **`null`** → le credentials (non servono, il JWT è già stato validato)
- **`authorities`** → i ruoli dell'utente (es. `ROLE_STUDENT`)

---

## 5. JwtAuthenticationEntryPoint — Errore 401

Viene invocato quando una richiesta arriva a un endpoint protetto (`authenticated()`) ma **non c'è un token valido** nel cookie.

Restituisce una risposta JSON con status 401:
```json
{
  "httpMethod": "GET",
  "error": {
    "title": "Non Autorizzato",
    "content": "Autenticazione richiesta. Fornire un token JWT valido."
  }
}
```

---

## 6. JwtAccessDeniedHandler — Errore 403

Viene invocato quando un utente **è autenticato** (il token JWT è valido) ma **non ha il ruolo/permesso** necessario per accedere a quella risorsa.

Restituisce una risposta JSON con status 403:
```json
{
  "httpMethod": "GET",
  "error": {
    "title": "Accesso Negato",
    "content": "Non si dispone dei permessi necessari per accedere a questa risorsa."
  }
}
```

---

## 7. Flusso completo di una richiesta

### Richiesta autenticata (es. `GET /api/auth/me`)

```
1. Browser invia GET /api/auth/me
   Cookie: token=eyJhbGci...

2. CorsFilter → controlla l'origin, aggiunge header CORS → OK

3. JwtAuthenticationFilter:
   - Legge il cookie "token" → trova il JWT
   - Controlla blacklist → non è in blacklist
   - Valida il JWT → valido, non scaduto
   - Estrae email="mario@example.com", role="STUDENT"
   - Crea Authentication e la mette nel SecurityContext

4. AuthorizationFilter:
   - /api/auth/me → richiede authenticated()
   - SecurityContext ha un'Authentication → OK, passa

5. AuthController.me() → viene eseguito → risponde 200
```

### Richiesta senza token (es. `GET /api/auth/me` senza cookie)

```
1. Browser invia GET /api/auth/me (senza cookie)

2. CorsFilter → OK

3. JwtAuthenticationFilter:
   - Legge il cookie "token" → null
   - Passa senza settare l'Authentication

4. AuthorizationFilter:
   - /api/auth/me → richiede authenticated()
   - SecurityContext è vuoto → BLOCCATO

5. JwtAuthenticationEntryPoint → risponde 401 JSON
```

### Richiesta pubblica (es. `POST /api/auth/login`)

```
1. Browser invia POST /api/auth/login (senza cookie, è una login)

2. CorsFilter → OK (preflight OPTIONS già gestita prima)

3. JwtAuthenticationFilter:
   - Nessun cookie → passa

4. AuthorizationFilter:
   - /api/auth/login → permitAll()
   - Passa anche senza autenticazione

5. AuthController.login() → viene eseguito → risponde 200 + Set-Cookie: token=...
```
