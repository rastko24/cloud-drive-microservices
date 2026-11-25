package com.example.notifications;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "notifikacije")
public class Notifikacija {

    @Id
    private String id;
    private String email;
    private String poruka;
    private Instant vremeKreiranja;

    public Notifikacija() {
    }

    public Notifikacija(String email, String poruka, Instant vremeKreiranja) {
        this.email = email;
        this.poruka = poruka;
        this.vremeKreiranja = vremeKreiranja;
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

    public String getPoruka() {
        return poruka;
    }

    public void setPoruka(String poruka) {
        this.poruka = poruka;
    }

    public Instant getVremeKreiranja() {
        return vremeKreiranja;
    }

    public void setVremeKreiranja(Instant vremeKreiranja) {
        this.vremeKreiranja = vremeKreiranja;
    }
}