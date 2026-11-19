
package com.cloud_drive.users_service.service;

import com.cloud_drive.users_service.dto.UserRegistrationDto;
import com.cloud_drive.users_service.model.User;
import com.cloud_drive.users_service.repository.UserRepository;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EmailService emailService;
    @Value("${app.blokada-minuta:15}")
    private int trajanjeBlokade;

    @Value("${app.reset-url}")
    private String adresaZaReset;

    public UserService(UserRepository userRepository, EmailService emailService) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }


    public User registerUser(UserRegistrationDto registrationDto) {

        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new RuntimeException("Korisnik sa ovim email-om vec postoji!");
        }

        String hashedLozinka = passwordEncoder.encode(registrationDto.getLozinka());
        Long defaultKvota = 5L * 1024 * 1024 * 1024;

        User newUser = new User(
            registrationDto.getEmail(),
            hashedLozinka,
            registrationDto.getIme(),
            registrationDto.getPrezime(),
            defaultKvota
        );

        newUser.setDatumPromeneLozinke(LocalDateTime.now());

        return userRepository.save(newUser);
    }

    


public boolean prijava(String email, String lozinka) {

    User korisnik = userRepository.findByEmail(email).orElse(null);

    if (korisnik == null || lozinka == null) {
        return false;
    }

    if (!passwordEncoder.matches(lozinka, korisnik.getLozinka())) {
        return false;
    }

    if (korisnik.getDatumPromeneLozinke() == null) {
        return false;
    }

    LocalDateTime sada = LocalDateTime.now();

  
    if (korisnik.getBlokiranDo() != null &&
        sada.isBefore(korisnik.getBlokiranDo())) {
        return false;
    }


    if (!sada.isBefore(
        korisnik.getDatumPromeneLozinke().plusDays(60))) {

        if (korisnik.getBlokiranDo() == null) {
            korisnik.setBlokiranDo(
                sada.plusMinutes(trajanjeBlokade)
            );

            userRepository.save(korisnik);
        }

        return false;
    }

    return true;
}



 
    public void posaljiOtp(String email) {

        User korisnik = userRepository.findByEmail(email).orElse(null);

        if (korisnik == null) {
            throw new RuntimeException("Korisnik ne postoji!");
        }

        SecureRandom generator = new SecureRandom();

        int broj = generator.nextInt(900000) + 100000;
        String kod = String.valueOf(broj);

        String sifrovanKod = passwordEncoder.encode(kod);

        korisnik.setOtpKod(sifrovanKod);
        korisnik.setOtpIstice(LocalDateTime.now().plusMinutes(5));
        korisnik.setBrojOtpPokusaja(0);

        userRepository.save(korisnik);

        emailService.posaljiKod(email, kod);
    }


    public boolean proveriOtp(String email, String kod) {

        User korisnik = userRepository.findByEmail(email).orElse(null);

        if (korisnik == null || kod == null) {
            return false;
        }

        if (korisnik.getOtpKod() == null ||
            korisnik.getOtpIstice() == null) {
            return false;
        }

        if (!LocalDateTime.now().isBefore(korisnik.getOtpIstice())) {
            return false;
        }

        if (korisnik.getBrojOtpPokusaja() >= 3) {
            return false;
        }

        if (!passwordEncoder.matches(kod, korisnik.getOtpKod())) {

            int pokusaji = korisnik.getBrojOtpPokusaja() + 1;
            korisnik.setBrojOtpPokusaja(pokusaji);

            if (pokusaji >= 3) {
                korisnik.setOtpKod(null);
                korisnik.setOtpIstice(null);
            }

            userRepository.save(korisnik);
            return false;
        }

        korisnik.setOtpKod(null);
        korisnik.setOtpIstice(null);
        korisnik.setBrojOtpPokusaja(0);

        userRepository.save(korisnik);

        return true;
    }

  
    public void promeniLozinku(String email, String staraLozinka, String novaLozinka) {

        User korisnik = userRepository.findByEmail(email).orElse(null);

        if (korisnik == null) {
            throw new RuntimeException("Korisnik ne postoji!");
        }

        if (staraLozinka == null ||
            !passwordEncoder.matches(staraLozinka, korisnik.getLozinka())) {
            throw new RuntimeException("Stara lozinka nije tacna!");
        }

        if (korisnik.getDatumPromeneLozinke() == null) {
            throw new RuntimeException("Datum postavljanja lozinke nije poznat!");
        }

        if (LocalDateTime.now().isBefore(
                korisnik.getDatumPromeneLozinke().plusDays(1))) {
            throw new RuntimeException("Lozinka mora biti stara najmanje 1 dan!");
        }

        if (staraLozinka.equals(novaLozinka)) {
            throw new RuntimeException("Nova lozinka mora biti drugacija!");
        }

        if (novaLozinka == null ||
            novaLozinka.length() < 12 ||
            !novaLozinka.matches(".*[A-Z].*") ||
            !novaLozinka.matches(".*[a-z].*") ||
            !novaLozinka.matches(".*[0-9].*") ||
            !novaLozinka.matches(".*[^a-zA-Z0-9].*")) {
            throw new RuntimeException("Nova lozinka nije dovoljno jaka!");
        }

        korisnik.setLozinka(passwordEncoder.encode(novaLozinka));
        korisnik.setDatumPromeneLozinke(LocalDateTime.now());
        korisnik.setBlokiranDo(null);
        userRepository.save(korisnik);
    }

    // ZAHTEV ZA RESET LOZINKE
    public void zatraziResetLozinke(String email) {

        User korisnik = userRepository.findByEmail(email).orElse(null);

        if (korisnik == null) {
            return;
        }

        SecureRandom generator = new SecureRandom();

        byte[] brojevi = new byte[32];
        generator.nextBytes(brojevi);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(brojevi);

        korisnik.setTokenResetLozinke(passwordEncoder.encode(token));
        korisnik.setTokenResetIstice(LocalDateTime.now().plusMinutes(15));

        userRepository.save(korisnik);

        String link = adresaZaReset
                + "?email=" + URLEncoder.encode(email, StandardCharsets.UTF_8)
                + "&token=" + token;

        emailService.posaljiLinkZaReset(email, link);
    }

public void resetujLozinku(String email, String token, String novaLozinka) {

    if (email == null || token == null || novaLozinka == null) {
        throw new RuntimeException("Nedostaju podaci!");
    }

    User korisnik = userRepository.findByEmail(email).orElse(null);

    if (korisnik == null) {
        throw new RuntimeException("Link nije ispravan ili je istekao!");
    }

    if (korisnik.getTokenResetLozinke() == null ||
        korisnik.getTokenResetIstice() == null) {
        throw new RuntimeException("Link nije ispravan ili je istekao!");
    }

    if (!LocalDateTime.now().isBefore(korisnik.getTokenResetIstice())) {
        throw new RuntimeException("Link nije ispravan ili je istekao!");
    }

    if (!passwordEncoder.matches(token, korisnik.getTokenResetLozinke())) {
        throw new RuntimeException("Link nije ispravan ili je istekao!");
    }

    if (novaLozinka.length() < 12 ||
        !novaLozinka.matches(".*[A-Z].*") ||
        !novaLozinka.matches(".*[a-z].*") ||
        !novaLozinka.matches(".*[0-9].*") ||
        !novaLozinka.matches(".*[^a-zA-Z0-9].*")) {
        throw new RuntimeException("Nova lozinka nije dovoljno jaka!");
    }

    if (passwordEncoder.matches(novaLozinka, korisnik.getLozinka())) {
        throw new RuntimeException("Nova lozinka mora biti drugacija od stare!");
    }

    korisnik.setLozinka(passwordEncoder.encode(novaLozinka));
    korisnik.setDatumPromeneLozinke(LocalDateTime.now());

    korisnik.setTokenResetLozinke(null);
    korisnik.setTokenResetIstice(null);


korisnik.setOtpKod(null);
korisnik.setOtpIstice(null);
korisnik.setBrojOtpPokusaja(0);


korisnik.setBlokiranDo(null);

userRepository.save(korisnik);

}

}
