package com.cloud_drive.users_service.controller;

import com.cloud_drive.users_service.dto.ZahtevKvotaDto;
import com.cloud_drive.users_service.model.ZahtevZaKvotu;
import com.cloud_drive.users_service.service.ZahtevKvotaService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class ZahtevKvotaController {

    private final ZahtevKvotaService zahtevKvotaService;

    public ZahtevKvotaController(ZahtevKvotaService zahtevKvotaService) {
        this.zahtevKvotaService = zahtevKvotaService;
    }

    @GetMapping("/kvota")
    public ResponseEntity<?> mojaKvota(Authentication prijava) {
        if (!ulogovan(prijava)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Morate biti prijavljeni.");
        }

        try {
            return ResponseEntity.ok(zahtevKvotaService.pregledKvote(prijava.getName()));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/zahtev-kvota")
    public ResponseEntity<?> podnesiZahtev(Authentication prijava, @RequestBody ZahtevKvotaDto podaci) {
        if (!ulogovan(prijava)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Morate biti prijavljeni.");
        }

        if (podaci == null || podaci.getZeljenaKvota() == null) {
            return ResponseEntity.badRequest().body("Unesite zeljenu kvotu.");
        }

        try {
            ZahtevZaKvotu sacuvan = zahtevKvotaService.podnesiZahtev(
                    prijava.getName(),
                    podaci.getZeljenaKvota()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(sacuvan);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/admin/zahtevi-kvota")
    public ResponseEntity<?> listaZahteva(Authentication prijava) {
        if (!ulogovan(prijava)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Morate biti prijavljeni.");
        }

        try {
            List<ZahtevZaKvotu> lista = zahtevKvotaService.zahteviNaCekanju(prijava.getName());
            return ResponseEntity.ok(lista);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping("/admin/zahtevi-kvota/{id}/odobri")
    public ResponseEntity<?> odobri(Authentication prijava, @PathVariable String id) {
        if (!ulogovan(prijava)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Morate biti prijavljeni.");
        }

        try {
            zahtevKvotaService.odobri(prijava.getName(), id);
            return ResponseEntity.ok("Zahtev je odobren.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/admin/zahtevi-kvota/{id}/odbij")
    public ResponseEntity<?> odbij(Authentication prijava, @PathVariable String id) {
        if (!ulogovan(prijava)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Morate biti prijavljeni.");
        }

        try {
            zahtevKvotaService.odbij(prijava.getName(), id);
            return ResponseEntity.ok("Zahtev je odbijen.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private boolean ulogovan(Authentication prijava) {
        return prijava != null && prijava.isAuthenticated();
    }
}