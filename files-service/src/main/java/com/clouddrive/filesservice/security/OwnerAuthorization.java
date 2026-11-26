package com.clouddrive.filesservice.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;
import java.util.Map;

@Component
public class OwnerAuthorization {
    private final RestClient users;
    private final String secret;

    public OwnerAuthorization(RestClient.Builder builder,
            @Value("${users.service.url}") String url,
            @Value("${internal.service.token:}") String secret) {
        this.users = builder.baseUrl(url).build();
        this.secret = secret;
    }

    public void requireOwner(String suppliedOwnerId, HttpServletRequest request) {
        if (secret.isBlank()) throw new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE, "Interna autentifikacija nije podesena");
        String cookie = request.getHeader(HttpHeaders.COOKIE);
        if (cookie == null || cookie.isBlank()) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Prijava je obavezna");
        try {
            @SuppressWarnings("unchecked")
            Map<String,Object> account = users.get().uri("/api/internal/auth/me")
                    .header("X-Internal-Token", secret)
                    .header(HttpHeaders.COOKIE, cookie)
                    .retrieve().body(Map.class);
            if (account == null || !(account.get("id") instanceof String id))
                throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Nevalidna sesija");
            if (!id.equals(suppliedOwnerId))
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nije dozvoljen pristup tudjim podacima");
        } catch (ResponseStatusException e) { throw e; }
          catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Sesija nije potvrdjena", e);
          }
    }
}
