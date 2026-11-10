
package com.cloud_drive.users_service.dto;

public class PromenaLozinkeDto {

    private String staraLozinka;
    private String novaLozinka;

    public PromenaLozinkeDto() {
    }

    public String getStaraLozinka() {
        return staraLozinka;
    }

    public void setStaraLozinka(String staraLozinka) {
        this.staraLozinka = staraLozinka;
    }

    public String getNovaLozinka() {
        return novaLozinka;
    }

    public void setNovaLozinka(String novaLozinka) {
        this.novaLozinka = novaLozinka;
    }
}
