package com.cloud_drive.users_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class UserRegistrationDto {
	
	@NotBlank(message = "Email je obavezan!")
	@Email(message = "Email adresa nije validna!")
	private String email;
	
	@NotBlank(message = "Lozinka je obavezna!")
	@Size(min = 6, message = "Lozinka mora imati najmanje 6 karaktera!")
    private String lozinka;
    
    @NotBlank(message = "Ime je obavezno!")
    private String ime;
    
    @NotBlank(message = "Prezime je obavezno!")
    private String prezime;

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
}