# Cloud Drive — dizajn sistema (zahtev 2.1)

Tim: **Rastko, Nataša, Filip**  
Tehnologije: **Angular**, **Spring Boot (Java)**, **MongoDB**, **Docker Compose**  
Cilj projekta: ocena **6** (SOA + NoSQL + Informaciona bezbednost prema specifikaciji)

Ovaj README je **jedinstveni izvor istine** za arhitekturu, modele podataka i komunikaciju. Pre implementacije pročitati ceo dokument.

---

## 1. Pregled sistema

Platforma za skladištenje i deljenje fajlova (slično Google Drive-u).  
Klijent (**Angular**) ne zove mikroservise direktno — sve ide preko **API Gateway-a**.

### 1.1 Vizuelni prikaz arhitekture

Slika (za Trello / odbranu):

![Arhitektura sistema](./arhitektura-sistema.png)

Isti prikaz u Mermaid formatu (GitHub ga renderuje):

```mermaid
flowchart TB
  subgraph klijent
    A[Angular :4200]
  end

  subgraph docker_compose
    GW[API Gateway :8080]
    US[Users Service :8081]
    FS[Files Service :8082]
    NS[Notifications Service :8083]
    M[(MongoDB :27017)]
    DISK[Folder za fajlove na disku]
  end

  A -->|REST JSON| GW
  GW --> US
  GW --> FS
  GW --> NS
  US --> M
  FS --> M
  NS --> M
  FS --> DISK
  FS -->|sinhrono HTTP provera kvote| US
```

### 1.2 Komponente i portovi

| Komponenta | Tehnologija | Port (host) | Napomena |
|------------|-------------|-------------|----------|
| Klijentska aplikacija | Angular | `4200` | Ne ocenjuje se, obavezna za demo |
| API Gateway | Spring Cloud Gateway | `8080` | Jedina ulazna tačka za front |
| Users Service | Spring Boot | `8081` | Auth, korisnici, kvota |
| Files Service | Spring Boot | `8082` | Folderi, datoteke, upload |
| Notifications Service | Spring Boot | `8083` | Ocena 6: ručno punjenje + GET |
| MongoDB | Docker `mongo` | `27017` | Jedna instanca, više baza/kolekcija |
| Sadržaj datoteka | Lokalni disk / volume | — | Metapodaci u Mongo; **object store = ocena 7** |

### 1.3 Ko šta implementira (logička podela)

| Servis | Odgovornost | Ne radi |
|--------|-------------|---------|
| **Users** | Registracija, prijava, OTP, magični link, lozinke (BCrypt), kvota, zahtevi za povećanje kvote, admin operacije nad kvotom | Foldere i fajlove |
| **Files** | Folderi, upload, verzije, preimenovanje, pregled sadržaja foldera, skladište bajtova na disku | Auth (samo prima `korisnikId` / proverava token na gateway-u) |
| **Notifications** | Lista notifikacija za korisnika | Automatsko generisanje (to je ocena 8+, za 6 dovoljno ručno u bazi) |
| **API Gateway** | Rutiranje, JWT provera (2.17), rate limit | Poslovna logika |

**Važno:** folderi i datoteke postoje **samo** u **Files** servisu. Kod u `users-service` koji liči na foldere treba ukloniti ili preseliti u `files-service` — ne duplirati domenu.

---

## 2. Modeli podataka (entiteti po servisu)

Za svaki entitet navedeni su atributi koje implementacija mora poštovati (mogu se dodati `_id` iz MongoDB-a).

### 2.1 Users Service — baza `users_db`

#### `Korisnik` (kolekcija: `korisnici`)

| Atribut | Tip | Opis |
|---------|-----|------|
| `id` | String | MongoDB ObjectId |
| `email` | String | Jedinstven, obavezan |
| `lozinka` | String | BCrypt hash (nikad plain text) |
| `ime` | String | |
| `prezime` | String | |
| `uloga` | String | `KORISNIK` ili `ADMIN` |
| `potvrdenNalog` | boolean | Registracija 1.1 — potvrda imejlom |
| `kvotaSkladista` | Long | Bajti; podrazumevano npr. 104857600 (100 MB) |
| `zauzetoSkladiste` | Long | Bajti; počinje od 0 |
| `datumPromeneLozinke` | LocalDateTime | Za pravilo „lozinka starija 1 dan“ |
| `datumIstekaLozinke` | LocalDateTime | Max 60 dana; na odbrani kraći interval |
| `blokiranDo` | LocalDateTime | null ako nije blokiran (auditabilnost 1.2) |
| `otpKod` | String | Jednokratna lozinka pri prijavi (null posle upotrebe) |
| `otpIstice` | LocalDateTime | |
| `magicLinkToken` | String | Povraćaj naloga 1.3 |
| `magicLinkIstice` | LocalDateTime | |

#### `ZahtevZaKvotu` (kolekcija: `zahtevi_kvota`) — zahtev 1.8

| Atribut | Tip | Opis |
|---------|-----|------|
| `id` | String | |
| `korisnikId` | String | Referenca na `korisnici.id` |
| `trazenaKvota` | Long | Željena ukupna kvota u bajtima |
| `status` | String | `NA_CEKANJU`, `ODOBREN`, `ODBIJEN` |
| `datumPodnosenja` | LocalDateTime | |
| `datumObrade` | LocalDateTime | Popunjava admin |

---

### 2.2 Files Service — baza `files_db`

#### `Folder` (kolekcija: `folderi`)

| Atribut | Tip | Opis |
|---------|-----|------|
| `id` | String | |
| `naziv` | String | |
| `vlasnikId` | String | ID iz Users servisa |
| `roditeljId` | String | null = koren korisnika (virtuelni root) |
| `datumKreiranja` | LocalDateTime | |

#### `Datoteka` (kolekcija: `datoteke`) — metapodaci

| Atribut | Tip | Opis |
|---------|-----|------|
| `id` | String | |
| `naziv` | String | Sa ekstenzijom |
| `tip` | String | MIME (whitelist na uploadu — 2.18) |
| `velicina` | Long | Bajti |
| `vlasnikId` | String | |
| `folderId` | String | |
| `putanjaSadrzaja` | String | Relativna putanja fajla na disku servisa |
| `verzija` | Integer | Počinje od 1; nova verzija uvećava |
| `vremeIzmene` | LocalDateTime | Poslednja verzija / upload |

**Koren korisnika:** pri registraciji (Users) opciono pozvati Files da kreira root folder, ili front šalje `roditeljId: null` pri prvom folderu — tim bira jednu varijantu i drži se nje.

---

### 2.3 Notifications Service — baza `notifications_db`

Za ocenu **6** (1.7): notifikacije se **ručno** ubacuju u MongoDB; servis samo vraća listu.

#### `Notifikacija` (kolekcija: `notifikacije`)

| Atribut | Tip | Opis |
|---------|-----|------|
| `id` | String | |
| `korisnikId` | String | Kome je namenjena |
| `poruka` | String | Tekst na srpskom |
| `tip` | String | npr. `SISTEM`, `DELJENJE` (za 6 dovoljno `SISTEM`) |
| `procitano` | boolean | |
| `datum` | LocalDateTime | |

---

### 2.4 Servisi van ocene 6 (referenca za kasnije)

| Servis | Baza (spec) | Kada |
|--------|-------------|------|
| Sharing | Graf baza | Ocena 8 — deljenje 1.10 |
| Analytics | — | Ocena 10+ |

Za ocenu 6 **ne implementirati** Sharing ni Analytics u dizajnu aktivnog sprinta.

---

## 3. Stilovi komunikacije

### 3.1 Klijent ↔ API Gateway

- **Stil:** sinhroni **REST**
- **Format:** JSON
- **Protokol:** HTTP u razvoju; HTTPS ka klijentu na višoj oceni IB (2.19)
- **Autentifikacija:** JWT u headeru `Authorization: Bearer <token>` posle prijave (2.17)

### 3.2 API Gateway ↔ mikroservisi

- Sinhrono prosleđivanje HTTP zahteva prema rutama (poglavlje 4)
- Gateway **ne sme** da preskače autorizaciju za RK/A rute

### 3.3 Mikroservis ↔ mikroservis (sinhrono) — zahtev 2.5

| Pozivalac | Pozvani | Kada | Metoda (predlog) |
|-----------|---------|------|------------------|
| Files | Users | Pre uploada / nove verzije | `GET /api/interno/korisnici/{id}/kvota?dodatnaVelicina={bajti}` → `{ "dozvoljeno": true/false, "preostalo": n }` |
| Files | Users | Posle uspešnog uploada | `PATCH /api/interno/korisnici/{id}/zauzece` telo `{ "dodajBajte": n }` |

Interni endpointi **nisu** izloženi kroz gateway ka javnom internetu — samo mreža Docker Compose (`users-service:8081`).

**Tehnologija:** OpenFeign ili `RestTemplate` u Files servisu; URL iz env: `USERS_SERVICE_URL`.

### 3.4 Asinhrona komunikacija

- **Ocena 6:** nije obavezna
- **Ocena 8+:** Kafka/RabbitMQ između Files → Sharing / Notifications (2.9)

---

## 4. API Gateway — rute (predlog)

Sve putanje ispod idu na host **`http://localhost:8080`**.

| Prefiks | Cilj servis | Primer |
|---------|-------------|--------|
| `/api/korisnici/**` | Users `:8081` | registracija, prijava |
| `/api/fajlovi/**`, `/api/folderi/**` | Files `:8082` | upload, lista foldera |
| `/api/notifikacije/**` | Notifications `:8083` | lista |

Primer `application.yml` (Gateway) — konfiguracija spolja (2.4):

```yaml
spring:
  cloud:
    gateway:
      routes:
        - id: users
          uri: ${USERS_SERVICE_URL:http://localhost:8081}
          predicates:
            - Path=/api/korisnici/**
        - id: files
          uri: ${FILES_SERVICE_URL:http://localhost:8082}
          predicates:
            - Path=/api/fajlovi/**,/api/folderi/**
        - id: notifications
          uri: ${NOTIFICATIONS_SERVICE_URL:http://localhost:8083}
          predicates:
            - Path=/api/notifikacije/**
```

---

## 5. REST API — pregled endpointa (plan implementacije)

Status: **plan** za tim; implementacija u odgovarajućim servisima.

### 5.1 Users Service (`/api/korisnici`)

| Metoda | Putanja | Uloga | Spec |
|--------|---------|-------|------|
| POST | `/registracija` | NK | 1.1 |
| GET | `/potvrda?token=` | NK | 1.1 potvrda imejlom |
| POST | `/prijava` | NK | 1.2 (lozinka → šalje OTP) |
| POST | `/prijava/otp` | NK | 1.2 |
| POST | `/odjava` | RK | 1.2 |
| POST | `/lozinka/promena` | RK | 1.2 |
| POST | `/lozinka/reset/zahtev` | NK | 1.2 |
| POST | `/lozinka/reset` | NK | 1.2 |
| GET | `/magic-link?token=` | NK | 1.3 |
| POST | `/kvota/zahtev` | RK | 1.8 |
| GET | `/admin/korisnici` | ADMIN | 1.8 |
| GET | `/admin/zahtevi-kvota` | ADMIN | 1.8 |
| POST | `/admin/zahtevi-kvota/{id}/odobri` | ADMIN | 1.8 |
| POST | `/admin/zahtevi-kvota/{id}/odbij` | ADMIN | 1.8 |

**Interno (samo Files → Users):**

| Metoda | Putanja | Opis |
|--------|---------|------|
| GET | `/api/interno/korisnici/{id}/kvota` | Provera pre uploada (2.5) |
| PATCH | `/api/interno/korisnici/{id}/zauzece` | Ažuriranje zauzeća |

### 5.2 Files Service (`/api/folderi`, `/api/fajlovi`)

| Metoda | Putanja | Uloga | Spec |
|--------|---------|-------|------|
| POST | `/api/folderi` | RK | 1.4 kreiranje |
| GET | `/api/folderi/{id}/sadrzaj` | RK | 1.6 pregled |
| PUT | `/api/folderi/{id}/naziv` | RK | 1.6 preimenovanje |
| POST | `/api/fajlovi/upload` | RK | 1.5 multipart |
| PUT | `/api/fajlovi/{id}/verzija` | RK | 1.5 nova verzija |
| PUT | `/api/fajlovi/{id}/naziv` | RK | 1.6 |

### 5.3 Notifications Service (`/api/notifikacije`)

| Metoda | Putanja | Uloga | Spec |
|--------|---------|-------|------|
| GET | `/api/notifikacije?korisnikId=` | RK | 1.7 |

---

## 6. Tokovi (sequence) — za odbranu

### 6.1 Upload datoteke sa proverom kvote (2.5)

```mermaid
sequenceDiagram
  participant K as Angular
  participant G as API Gateway
  participant F as Files Service
  participant U as Users Service

  K->>G: POST /api/fajlovi/upload
  G->>F: prosledi zahtev + JWT
  F->>U: GET interno/kvota?dodatnaVelicina=N
  U-->>F: dozvoljeno / nije
  alt nema kvote
    F-->>G: 400
    G-->>K: greška
  else ima kvote
    F->>F: snimi na disk + Mongo metapodaci
    F->>U: PATCH interno/zauzece
    F-->>G: 201
    G-->>K: uspeh
  end
```

### 6.2 Prijava sa OTP (1.2)

1. Korisnik šalje email + lozinku → Users proverava BCrypt.  
2. Users generiše OTP, čuva `otpKod` / `otpIstice`, šalje imejl (MailHog u dev-u).  
3. Korisnik šalje OTP → Users vraća JWT.

---

## 7. Eksterna konfiguracija (2.4) — environment promenljive

| Promenljiva | Primer | Ko je koristi |
|-------------|--------|---------------|
| `MONGODB_URI` | `mongodb://admin:password@mongodb:27017` | svi servisi |
| `USERS_SERVICE_URL` | `http://users-service:8081` | Gateway, Files |
| `FILES_SERVICE_URL` | `http://files-service:8082` | Gateway |
| `NOTIFICATIONS_SERVICE_URL` | `http://notifications-service:8083` | Gateway |
| `JWT_TAJNA` | (tajna) | Gateway, Users |
| `FILES_STORAGE_PATH` | `/data/fajlovi` | Files |
| `MAIL_HOST`, `MAIL_PORT` | MailHog | Users |

Ništa od navedenog ne hardkodovati u Java kod — `application.properties` sa `${...}`.

---

## 8. Mapiranje: ocena 6 ↔ ovaj dizajn

| Zahtev | Gde u sistemu |
|--------|----------------|
| 2.1 Dizajn | Ovaj README + dijagram |
| 2.2 API Gateway | Poglavlje 4 |
| 2.3 Docker Compose | Mongo + svi servisi (gateway dodaje tim) |
| 2.4 Eksterna konfig | Poglavlje 7 |
| 2.5 Sinhrona kvota | Poglavlje 3.3, 6.1 |
| 1.1–1.2, 1.3, 1.8 | Users + Angular |
| 1.4–1.6, 1.5 | Files + Angular |
| 1.7 | Notifications + Angular |
| 2.17–2.19 (IB 6) | JWT, validacija, BCrypt — Users + Gateway + Angular |

---

## 9. Checklist — završetak taska 2.1 (Trello Done)

- [x] Dijagram arhitekture (`arhitektura-sistema.png` + Mermaid)
- [x] Lista komponenti i portova
- [x] Entiteti po servisu sa atributima
- [x] Stilovi komunikacije (REST + sinhroni poziv Files → Users)
- [x] Plan REST API-ja i rute gateway-a
- [x] Mapiranje na specifikaciju ocene 6
- [ ] Tim pročitao i dogovorio se (komentar na Trello kartici)

---

## 10. Pokretanje (referenca — dopunjava se sa Docker Compose)

```bash
docker compose up -d mongodb
# users-service :8081, files-service :8082, gateway :8080 — kada budu spremni
```

Mongo trenutno: korisnik `admin`, lozinka `password`, port `27017` (vidi `docker-compose.yml`).

---

*Poslednja izmena dokumenta: dizajn sistema 2.1 — spremno za implementaciju sprinta.*
