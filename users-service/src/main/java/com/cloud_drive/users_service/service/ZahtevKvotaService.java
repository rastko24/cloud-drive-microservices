package com.cloud_drive.users_service.service;

import com.cloud_drive.users_service.dto.KvotaPregledDto;
import com.cloud_drive.users_service.model.User;
import com.cloud_drive.users_service.model.ZahtevZaKvotu;
import com.cloud_drive.users_service.repository.UserRepository;
import com.cloud_drive.users_service.repository.ZahtevZaKvotuRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class ZahtevKvotaService {

    public static final String STATUS_NA_CEKANJU = "NA_CEKANJU";
    public static final String STATUS_ODOBREN = "ODOBREN";
    public static final String STATUS_ODBIJEN = "ODBIJEN";

    private final UserRepository userRepository;
    private final ZahtevZaKvotuRepository zahtevRepository;

    public ZahtevKvotaService(UserRepository userRepository, ZahtevZaKvotuRepository zahtevRepository) {
        this.userRepository = userRepository;
        this.zahtevRepository = zahtevRepository;
    }

    public KvotaPregledDto pregledKvote(String email) {
        User korisnik = nadjiKorisnika(email);
        long kvota = vrednost(korisnik.getKvotaSkladista());
        long zauzeto = vrednost(korisnik.getZauzetoSkladiste());
        long slobodno = Math.max(0L, kvota - zauzeto);
        return new KvotaPregledDto(kvota, zauzeto, slobodno, korisnik.isAdmin());
    }

    public ZahtevZaKvotu podnesiZahtev(String email, Long zeljenaKvota) {
        if (zeljenaKvota == null || zeljenaKvota <= 0) {
            throw new RuntimeException("Unesite ispravnu zeljenu kvotu.");
        }

        User korisnik = nadjiKorisnika(email);
        long trenutna = vrednost(korisnik.getKvotaSkladista());
        long zauzeto = vrednost(korisnik.getZauzetoSkladiste());

        if (zeljenaKvota <= trenutna) {
            throw new RuntimeException("Nova kvota mora biti veca od trenutne.");
        }

        if (zeljenaKvota < zauzeto) {
            throw new RuntimeException("Nova kvota ne sme biti manja od zauzetog prostora.");
        }

        if (zahtevRepository.findByKorisnikIdAndStatus(korisnik.getId(), STATUS_NA_CEKANJU).isPresent()) {
            throw new RuntimeException("Vec imate zahtev na cekanju.");
        }

        ZahtevZaKvotu zahtev = new ZahtevZaKvotu();
        zahtev.setKorisnikId(korisnik.getId());
        zahtev.setEmailKorisnika(korisnik.getEmail());
        zahtev.setZeljenaKvota(zeljenaKvota);
        zahtev.setStatus(STATUS_NA_CEKANJU);
        zahtev.setKreiran(LocalDateTime.now());
        return zahtevRepository.save(zahtev);
    }

    public List<ZahtevZaKvotu> zahteviNaCekanju(String emailAdmina) {
        proveriAdmin(emailAdmina);
        return zahtevRepository.findByStatusOrderByKreiranAsc(STATUS_NA_CEKANJU);
    }

    public void odobri(String emailAdmina, String zahtevId) {
        proveriAdmin(emailAdmina);
        ZahtevZaKvotu zahtev = nadjiZahtev(zahtevId);

        if (!STATUS_NA_CEKANJU.equals(zahtev.getStatus())) {
            throw new RuntimeException("Zahtev nije na cekanju.");
        }

        User korisnik = userRepository.findById(zahtev.getKorisnikId())
                .orElseThrow(() -> new RuntimeException("Korisnik ne postoji."));

        long zauzeto = vrednost(korisnik.getZauzetoSkladiste());
        if (zahtev.getZeljenaKvota() < zauzeto) {
            throw new RuntimeException("Zeljena kvota je manja od zauzetog prostora.");
        }

        korisnik.setKvotaSkladista(zahtev.getZeljenaKvota());
        userRepository.save(korisnik);

        zahtev.setStatus(STATUS_ODOBREN);
        zahtevRepository.save(zahtev);
    }

    public void odbij(String emailAdmina, String zahtevId) {
        proveriAdmin(emailAdmina);
        ZahtevZaKvotu zahtev = nadjiZahtev(zahtevId);

        if (!STATUS_NA_CEKANJU.equals(zahtev.getStatus())) {
            throw new RuntimeException("Zahtev nije na cekanju.");
        }

        zahtev.setStatus(STATUS_ODBIJEN);
        zahtevRepository.save(zahtev);
    }

    private User nadjiKorisnika(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Korisnik ne postoji."));
    }

    private ZahtevZaKvotu nadjiZahtev(String id) {
        return zahtevRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Zahtev ne postoji."));
    }

    private void proveriAdmin(String email) {
        User korisnik = nadjiKorisnika(email);
        if (!korisnik.isAdmin()) {
            throw new RuntimeException("Samo administrator moze ovu akciju.");
        }
    }

    private long vrednost(Long broj) {
        return broj == null ? 0L : broj;
    }
}