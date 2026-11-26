	package com.cloud_drive.users_service.service;

import com.cloud_drive.users_service.dto.UserRegistrationDto;
import com.cloud_drive.users_service.model.User;
import com.cloud_drive.users_service.repository.UserRepository;
import com.cloud_drive.users_service.model.MagicLoginToken;
import com.cloud_drive.users_service.repository.MagicLoginTokenRepository;

import java.security.SecureRandom;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
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
    private final MagicLoginTokenRepository magicTokenRepository;

    @Value("${app.magic-login-url:http://localhost:4200/magic-login}")
    private String magicLoginUrl;
    @Value("${app.blokada-minuta:15}")
    private int trajanjeBlokade;

    @Value("${app.reset-url}")
    private String adresaZaReset;

    public UserService(UserRepository userRepository, EmailService emailService, MagicLoginTokenRepository magicTokenRepository) {
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.magicTokenRepository = magicTokenRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }


    public User registerUser(UserRegistrationDto registrationDto) {

        if (registrationDto.getPonovljenaLozinka() == null ||
            !registrationDto.getLozinka().equals(registrationDto.getPonovljenaLozinka())) {
            throw new RuntimeException("Lozinke se ne poklapaju!");
        }

        String lozinka = registrationDto.getLozinka();
        if (lozinka == null ||
            lozinka.length() < 12 ||
            !lozinka.matches(".*[A-Z].*") ||
            !lozinka.matches(".*[a-z].*") ||
            !lozinka.matches(".*[0-9].*") ||
            !lozinka.matches(".*[^a-zA-Z0-9].*")) {
            throw new RuntimeException("Lozinka mora imati najmanje 12 karaktera, veliko slovo, malo slovo, broj i specijalan znak!");
        }

        User postojeci = userRepository.findByEmail(registrationDto.getEmail()).orElse(null);

        if (postojeci != null && postojeci.isVerifikovan()) {
            throw new RuntimeException("Korisnik sa ovim email-om vec postoji!");
        }

        String hashedLozinka = passwordEncoder.encode(lozinka);
        Long defaultKvota = 5L * 1024 * 1024 * 1024;

        User korisnik = (postojeci != null) ? postojeci : new User();
        korisnik.setEmail(registrationDto.getEmail());
        korisnik.setLozinka(hashedLozinka);
        korisnik.setIme(registrationDto.getIme());
        korisnik.setPrezime(registrationDto.getPrezime());
        korisnik.setKvotaSkladista(defaultKvota);
        korisnik.setZauzetoSkladiste(0L);
        korisnik.setDatumPromeneLozinke(LocalDateTime.now());
        korisnik.setVerifikovan(false);

        SecureRandom generator = new SecureRandom();
        int broj = generator.nextInt(900000) + 100000;
        String kod = String.valueOf(broj);

        korisnik.setKodPotvrdeRegistracije(passwordEncoder.encode(kod));
        korisnik.setKodPotvrdeIstice(LocalDateTime.now().plusMinutes(15));
        korisnik.setBrojPokusajaPotvrde(0);

        User sacuvan = userRepository.save(korisnik);
        emailService.posaljiKodZaPotvrduRegistracije(sacuvan.getEmail(), kod);

        return sacuvan;
    }

    public void potvrdiRegistraciju(String email, String kod) {
        User korisnik = userRepository.findByEmail(email).orElse(null);

        if (korisnik == null || kod == null || kod.isBlank()) {
            throw new RuntimeException("Neispravan email ili kod za potvrdu!");
        }

        if (korisnik.isVerifikovan()) {
            return;
        }

        if (korisnik.getKodPotvrdeRegistracije() == null ||
            korisnik.getKodPotvrdeIstice() == null ||
            !LocalDateTime.now().isBefore(korisnik.getKodPotvrdeIstice())) {
            throw new RuntimeException("Kod za potvrdu je istekao! Zatrazite novi kod.");
        }

        if (korisnik.getBrojPokusajaPotvrde() >= 3) {
            throw new RuntimeException("Previse neuspelih pokusaja! Zatrazite novi kod.");
        }

        if (!passwordEncoder.matches(kod.trim(), korisnik.getKodPotvrdeRegistracije())) {
            int pokusaji = korisnik.getBrojPokusajaPotvrde() + 1;
            korisnik.setBrojPokusajaPotvrde(pokusaji);
            if (pokusaji >= 3) {
                korisnik.setKodPotvrdeRegistracije(null);
                korisnik.setKodPotvrdeIstice(null);
            }
            userRepository.save(korisnik);
            throw new RuntimeException("Pogresan kod za potvrdu!");
        }

        korisnik.setVerifikovan(true);
        korisnik.setKodPotvrdeRegistracije(null);
        korisnik.setKodPotvrdeIstice(null);
        korisnik.setBrojPokusajaPotvrde(0);
        userRepository.save(korisnik);
    }

    public void ponovoPosaljiKodRegistracije(String email) {
        User korisnik = userRepository.findByEmail(email).orElse(null);

        if (korisnik == null) {
            throw new RuntimeException("Korisnik sa ovim email-om ne postoji!");
        }

        if (korisnik.isVerifikovan()) {
            throw new RuntimeException("Nalog je vec potvrdjen!");
        }

        SecureRandom generator = new SecureRandom();
        int broj = generator.nextInt(900000) + 100000;
        String kod = String.valueOf(broj);

        korisnik.setKodPotvrdeRegistracije(passwordEncoder.encode(kod));
        korisnik.setKodPotvrdeIstice(LocalDateTime.now().plusMinutes(15));
        korisnik.setBrojPokusajaPotvrde(0);
        userRepository.save(korisnik);

        emailService.posaljiKodZaPotvrduRegistracije(email, kod);
    }

	public boolean prijava(String email, String lozinka) {
	
	    User korisnik = userRepository.findByEmail(email).orElse(null);
	
	    if (korisnik == null || lozinka == null) {
	        return false;
	    }
	
	    if (!passwordEncoder.matches(lozinka, korisnik.getLozinka())) {
	        return false;
	    }
	    
	    if (!korisnik.isVerifikovan()) {
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

    public void zatraziMagicLink(String email) {
        if (email == null || email.isBlank()) return;
        String normalizovan = email.trim().toLowerCase(java.util.Locale.ROOT);
        User korisnik = userRepository.findByEmail(normalizovan).orElse(null);
        if (korisnik == null || !korisnik.isVerifikovan()) return;

        byte[] nasumicni = new byte[32];
        new SecureRandom().nextBytes(nasumicni);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(nasumicni);
        magicTokenRepository.save(new MagicLoginToken(otisakTokena(token), normalizovan,
                Instant.now().plusSeconds(15 * 60), false));

        String link = magicLoginUrl + "?email=" + URLEncoder.encode(normalizovan, StandardCharsets.UTF_8)
                + "&token=" + URLEncoder.encode(token, StandardCharsets.UTF_8);
        emailService.posaljiMagicLink(normalizovan, link);
    }

    public User prijaviMagicLinkom(String email, String token) {
        if (email == null || email.isBlank() || token == null || token.isBlank()) {
            throw new RuntimeException("Link nije ispravan ili je istekao.");
        }
        String normalizovan = email.trim().toLowerCase(java.util.Locale.ROOT);
        MagicLoginToken zapis = magicTokenRepository
                .findByTokenAndEmailAndUsedFalse(otisakTokena(token), normalizovan)
                .orElseThrow(() -> new RuntimeException("Link nije ispravan ili je istekao."));
        if (zapis.getExpiresAt() == null || !Instant.now().isBefore(zapis.getExpiresAt())) {
            throw new RuntimeException("Link nije ispravan ili je istekao.");
        }
        User korisnik = userRepository.findByEmail(normalizovan)
                .orElseThrow(() -> new RuntimeException("Link nije ispravan ili je istekao."));
        if (!korisnik.isVerifikovan()) {
            throw new RuntimeException("Nalog nije potvrdjen.");
        }
        zapis.setUsed(true);
        magicTokenRepository.save(zapis);
        return korisnik;
    }

    private String otisakTokena(String token) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 nije dostupan", e);
        }
    }

}
