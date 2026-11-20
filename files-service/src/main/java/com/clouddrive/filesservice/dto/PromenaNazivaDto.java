package com.clouddrive.filesservice.dto;

import jakarta.validation.constraints.NotBlank;

public class PromenaNazivaDto {

    @NotBlank(message = "Naziv je obavezan!")
    private String naziv;

    @NotBlank(message = "ID vlasnika je obavezan!")
    private String vlasnikId;

    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }
    public String getVlasnikId() { return vlasnikId; }
    public void setVlasnikId(String vlasnikId) { this.vlasnikId = vlasnikId; }
}