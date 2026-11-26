package com.cloud_drive.users_service.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "magic_login_tokens")
public class MagicLoginToken {

    @Id
    private String id;

    @Indexed(unique = true)
    private String token;

    private String email;

    private Instant expiresAt;

    private boolean used;

    public MagicLoginToken() {
    }

    public MagicLoginToken(String token, String email, Instant expiresAt, boolean used) {
        this.token = token;
        this.email = email;
        this.expiresAt = expiresAt;
        this.used = used;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
}