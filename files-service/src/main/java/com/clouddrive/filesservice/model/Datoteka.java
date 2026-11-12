package com.clouddrive.filesservice.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Document(collection = "datoteke")
public class Datoteka {

    @Id
    private String id;
    private String naziv;
    private String tip;
    private Long velicina;
    private String vlasnikId;
    private String folderId;
    private LocalDateTime vremeIzmene;

    public Datoteka() {
        this.vremeIzmene = LocalDateTime.now();
        this.velicina = 0L;
        this.tip = "application/octet-stream";
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }
    public String getTip() { return tip; }
    public void setTip(String tip) { this.tip = tip; }
    public Long getVelicina() { return velicina; }
    public void setVelicina(Long velicina) { this.velicina = velicina; }
    public String getVlasnikId() { return vlasnikId; }
    public void setVlasnikId(String vlasnikId) { this.vlasnikId = vlasnikId; }
    public String getFolderId() { return folderId; }
    public void setFolderId(String folderId) { this.folderId = folderId; }
    public LocalDateTime getVremeIzmene() { return vremeIzmene; }
    public void setVremeIzmene(LocalDateTime vremeIzmene) { this.vremeIzmene = vremeIzmene; }
}