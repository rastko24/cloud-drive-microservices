package com.cloud_drive.users_service.controller;

import com.cloud_drive.users_service.model.User;
import com.cloud_drive.users_service.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class MagicLinkController {

    private final UserService userService;

    public MagicLinkController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/zatrazi-magic-link")
    public ResponseEntity<?> zatraziMagicLink(@RequestBody Map<String, String> podaci) {
        if (podaci == null || podaci.get("email") == null || podaci.get("email").isBlank()) {
            return ResponseEntity.badRequest().body("Unesite email adresu!");
        }

        userService.zatraziMagicLink(podaci.get("email"));
        return ResponseEntity.ok("Ako nalog postoji, poslali smo magicni link na email.");
    }

    @PostMapping("/magic-login")
    public ResponseEntity<?> magicLogin(
            @RequestBody Map<String, String> podaci,
            HttpServletRequest zahtev,
            HttpServletResponse odgovor) {

        if (podaci == null || podaci.get("email") == null || podaci.get("token") == null) {
            return ResponseEntity.badRequest().body("Nedostaju email i token!");
        }

        try {
            User korisnik = userService.prijaviMagicLinkom(podaci.get("email"), podaci.get("token"));

            zahtev.getSession();
            zahtev.changeSessionId();

            UsernamePasswordAuthenticationToken prijava =
                new UsernamePasswordAuthenticationToken(
                    korisnik.getEmail(),
                    null,
                    Collections.emptyList()
                );

            SecurityContext kontekst = SecurityContextHolder.createEmptyContext();
            kontekst.setAuthentication(prijava);
            SecurityContextHolder.setContext(kontekst);

            HttpSessionSecurityContextRepository repozitorijum =
                new HttpSessionSecurityContextRepository();

            repozitorijum.saveContext(kontekst, zahtev, odgovor);

            return ResponseEntity.ok("Uspesno ste prijavljeni!");
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
        }
    }
}