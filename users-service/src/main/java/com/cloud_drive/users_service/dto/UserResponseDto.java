package com.cloud_drive.users_service.dto;

public class UserResponseDto {
    private String id;
    private String email;
    private String ime;
    private String prezime;
    private Long kvotaSkladista;
    private Long zauzetoSkladiste;

    public UserResponseDto(String id, String email, String ime, String prezime, Long kvotaSkladista, Long zauzetoSkladiste) {
        this.id = id;
        this.email = email;
        this.ime = ime;
        this.prezime = prezime;
        this.kvotaSkladista = kvotaSkladista;
        this.zauzetoSkladiste = zauzetoSkladiste;
    }

    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getIme() { return ime; }
    public String getPrezime() { return prezime; }
    public Long getKvotaSkladista() { return kvotaSkladista; }
    public Long getZauzetoSkladiste() { return zauzetoSkladiste; }
}