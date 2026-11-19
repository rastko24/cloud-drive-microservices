
package com.cloud_drive.users_service.controller;

import com.cloud_drive.users_service.dto.*;
import com.cloud_drive.users_service.model.User;
import com.cloud_drive.users_service.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {

        try {
            User registeredUser = userService.registerUser(registrationDto);

            UserResponseDto responseDto = new UserResponseDto(
                registeredUser.getId(),
                registeredUser.getEmail(),
                registeredUser.getIme(),
                registeredUser.getPrezime(),
                registeredUser.getKvotaSkladista(),
                registeredUser.getZauzetoSkladiste()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> prijava(@RequestBody LoginDto podaci) {

        if (podaci == null || podaci.getEmail() == null ||
                podaci.getLozinka() == null) {
            return ResponseEntity.badRequest().body("Unesite email i lozinku!");
        }

        boolean uspesnaPrijava = userService.prijava(
            podaci.getEmail(),
            podaci.getLozinka()
        );

        if (!uspesnaPrijava) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Pogresan email ili lozinka!");
        }

        userService.posaljiOtp(podaci.getEmail());

        return ResponseEntity.ok("OTP kod je poslat na email!");
    }

    @PostMapping("/proveri-otp")
    public ResponseEntity<?> proveriOtp(
            @RequestBody OtpDto podaci,
            HttpServletRequest zahtev,
            HttpServletResponse odgovor) {

        if (podaci == null || podaci.getEmail() == null ||
                podaci.getKod() == null) {
            return ResponseEntity.badRequest().body("Unesite email i OTP kod!");
        }

        boolean ispravanKod = userService.proveriOtp(
            podaci.getEmail(),
            podaci.getKod()
        );

        if (!ispravanKod) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Pogresan ili istekao OTP kod!");
        }

        zahtev.getSession();
        zahtev.changeSessionId();

        UsernamePasswordAuthenticationToken prijava =
            new UsernamePasswordAuthenticationToken(
                podaci.getEmail(),
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
    }

    @PostMapping("/logout")
    public ResponseEntity<?> odjava(
            HttpServletRequest zahtev,
            HttpServletResponse odgovor) {

        Authentication prijava = SecurityContextHolder.getContext()
                .getAuthentication();

        new SecurityContextLogoutHandler().logout(
            zahtev, odgovor, prijava
        );

        return ResponseEntity.ok("Uspesno ste se odjavili!");
    }

    @PostMapping("/promeni-lozinku")
    public ResponseEntity<?> promeniLozinku(
            @RequestBody PromenaLozinkeDto podaci,
            Authentication prijava) {

        if (prijava == null || !prijava.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Morate biti prijavljeni!");
        }

        try {
            userService.promeniLozinku(
                prijava.getName(),
                podaci.getStaraLozinka(),
                podaci.getNovaLozinka()
            );

            return ResponseEntity.ok("Lozinka je uspesno promenjena!");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/zatrazi-reset")
    public ResponseEntity<?> zatraziReset(@RequestBody Map<String, String> podaci) {

        if (podaci == null || podaci.get("email") == null ||
                podaci.get("email").isBlank()) {
            return ResponseEntity.badRequest().body("Unesite email adresu!");
        }

        userService.zatraziResetLozinke(podaci.get("email"));

        return ResponseEntity.ok(
            "Ako nalog postoji, poslali smo link za resetovanje lozinke."
        );
    }

    @PostMapping("/resetuj-lozinku")
    public ResponseEntity<?> resetujLozinku(@RequestBody ResetLozinkeDto podaci) {

        try {
            userService.resetujLozinku(
                podaci.getEmail(),
                podaci.getToken(),
                podaci.getNovaLozinka()
            );

            return ResponseEntity.ok("Lozinka je uspesno promenjena!");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
