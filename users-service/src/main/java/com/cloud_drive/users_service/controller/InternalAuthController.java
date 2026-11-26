package com.cloud_drive.users_service.controller;

import com.cloud_drive.users_service.model.User;
import com.cloud_drive.users_service.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;

@RestController
@RequestMapping("/api/internal/auth")
public class InternalAuthController {
    private final UserRepository users;
    private final String internalToken;

    public InternalAuthController(UserRepository users,
            @Value("${internal.service.token:}") String internalToken) {
        this.users = users;
        this.internalToken = internalToken;
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(Authentication authentication,
            @RequestHeader(value = "X-Internal-Token", required = false) String token) {
        if (internalToken.isBlank() || token == null || !MessageDigest.isEqual(
                internalToken.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        User user = users.findByEmail(authentication.getName()).orElse(null);
        if (user == null || !user.isVerifikovan()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(Map.of("id", user.getId()));
    }
}
