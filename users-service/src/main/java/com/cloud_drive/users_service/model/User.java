package com.cloud_drive.users_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {

    @Id
    private String id;
    private String email;
    private String lozinka;
    private String ime;
    private String prezime;
    private Long kvotaSkladista;
    private Long zauzetoSkladiste;

    public User() {
    }

    public User(String email, String lozinka, String ime, String prezime, Long kvotaSkladista) {
        this.email = email;
        this.lozinka = lozinka;
        this.ime = ime;
        this.prezime = prezime;
        this.kvotaSkladista = kvotaSkladista;
        this.zauzetoSkladiste = 0L;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getLozinka() {
        return lozinka;
    }

    public void setLozinka(String lozinka) {
        this.lozinka = lozinka;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public Long getKvotaSkladista() {
        return kvotaSkladista;
    }

    public void setKvotaSkladista(Long kvotaSkladista) {
        this.kvotaSkladista = kvotaSkladista;
    }

    public Long getZauzetoSkladiste() {
        return zauzetoSkladiste;
    }

    public void setZauzetoSkladiste(Long zauzetoSkladiste) {
        this.zauzetoSkladiste = zauzetoSkladiste;
    }
}