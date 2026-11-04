package com.clouddrive.filesservice.dto;

import jakarta.validation.constraints.NotBlank;

public class FolderCreateDto {

    @NotBlank(message = "Naziv foldera je obavezan!")
    private String naziv;

    @NotBlank(message = "ID vlasnika je obavezan!")
    private String vlasnikId;

    private String roditeljId;

    public String getNaziv() { return naziv; }
    public void setNaziv(String naziv) { this.naziv = naziv; }

    public String getVlasnikId() { return vlasnikId; }
    public void setVlasnikId(String vlasnikId) { this.vlasnikId = vlasnikId; }

    public String getRoditeljId() { return roditeljId; }
    public void setRoditeljId(String roditeljId) { this.roditeljId = roditeljId; }
}