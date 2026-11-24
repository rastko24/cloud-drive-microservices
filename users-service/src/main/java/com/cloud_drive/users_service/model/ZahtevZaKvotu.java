package com.cloud_drive.users_service.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "zahtevi_kvota")
public class ZahtevZaKvotu {

    @Id
    private String id;
    private String korisnikId;
    private String emailKorisnika;
    private Long zeljenaKvota;
    private String status;
    private LocalDateTime kreiran;

    public ZahtevZaKvotu() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getKorisnikId() {
        return korisnikId;
    }

    public void setKorisnikId(String korisnikId) {
        this.korisnikId = korisnikId;
    }

    public String getEmailKorisnika() {
        return emailKorisnika;
    }

    public void setEmailKorisnika(String emailKorisnika) {
        this.emailKorisnika = emailKorisnika;
    }

    public Long getZeljenaKvota() {
        return zeljenaKvota;
    }

    public void setZeljenaKvota(Long zeljenaKvota) {
        this.zeljenaKvota = zeljenaKvota;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getKreiran() {
        return kreiran;
    }

    public void setKreiran(LocalDateTime kreiran) {
        this.kreiran = kreiran;
    }
}