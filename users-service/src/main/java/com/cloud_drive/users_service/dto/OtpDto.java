
package com.cloud_drive.users_service.dto;

public class OtpDto {

    private String email;
    private String kod;

    public OtpDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getKod() {
        return kod;
    }

    public void setKod(String kod) {
        this.kod = kod;
    }
}
