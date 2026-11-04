# Dijagram arhitekture — zahtev 2.1 (US-2.1.1)

Kopiraj Mermaid u [https://mermaid.live](https://mermaid.live) i exportuj PNG ako želiš da zameniš `arhitektura-sistema.png`.

## Arhitektura komponenti

```mermaid
flowchart LR
  subgraph Eksterno
    FE[Angular 4200]
  end

  subgraph Backend_Docker
    GW[API Gateway 8080]
    US[Users 8081]
    FS[Files 8082]
    NS[Notifications 8083]
    DB[(MongoDB 27017)]
    VOL[(Volume /data/fajlovi)]
  end

  FE -->|HTTPS/HTTP REST| GW
  GW --> US
  GW --> FS
  GW --> NS
  US --> DB
  FS --> DB
  NS --> DB
  FS --> VOL
  FS -.->|Feign GET/PATCH kvota| US
```

## Lista portova (za odbranu)

| Port | Servis |
|------|--------|
| 4200 | Angular |
| 8080 | API Gateway |
| 8081 | Users |
| 8082 | Files |
| 8083 | Notifications |
| 27017 | MongoDB |

## Mongo — logičke baze

| Baza | Servis | Kolekcije |
|------|--------|-----------|
| `users_db` | Users | `korisnici`, `zahtevi_kvota` |
| `files_db` | Files | `folderi`, `datoteke` |
| `notifications_db` | Notifications | `notifikacije` |
