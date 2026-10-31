package com.clouddrive.filesservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "folders")
public class Folder {

    @Id
    private String id;
    private String naziv;
    private String vlasnikId;
    private String roditeljId;
    private LocalDateTime datumKreiranja;

    public Folder() {
    }

    public Folder(String id, String naziv, String vlasnikId, String roditeljId, LocalDateTime datumKreiranja) {
        this.id = id;
        this.naziv = naziv;
        this.vlasnikId = vlasnikId;
        this.roditeljId = roditeljId;
        this.datumKreiranja = datumKreiranja;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public String getVlasnikId() {
        return vlasnikId;
    }

    public void setVlasnikId(String vlasnikId) {
        this.vlasnikId = vlasnikId;
    }

    public String getRoditeljId() {
        return roditeljId;
    }

    public void setRoditeljId(String roditeljId) {
        this.roditeljId = roditeljId;
    }

    public LocalDateTime getDatumKreiranja() {
        return datumKreiranja;
    }

    public void setDatumKreiranja(LocalDateTime datumKreiranja) {
        this.datumKreiranja = datumKreiranja;
    }
}