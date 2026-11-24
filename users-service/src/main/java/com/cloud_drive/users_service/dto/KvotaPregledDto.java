package com.cloud_drive.users_service.dto;

public class KvotaPregledDto {

    private Long kvotaSkladista;
    private Long zauzetoSkladiste;
    private Long slobodnoProstora;
    private boolean admin;

    public KvotaPregledDto() {
    }

    public KvotaPregledDto(Long kvotaSkladista, Long zauzetoSkladiste, Long slobodnoProstora, boolean admin) {
        this.kvotaSkladista = kvotaSkladista;
        this.zauzetoSkladiste = zauzetoSkladiste;
        this.slobodnoProstora = slobodnoProstora;
        this.admin = admin;
    }

    public Long getKvotaSkladista() {
        return kvotaSkladista;
    }

    public Long getZauzetoSkladiste() {
        return zauzetoSkladiste;
    }

    public Long getSlobodnoProstora() {
        return slobodnoProstora;
    }

    public boolean isAdmin() {
        return admin;
    }
}