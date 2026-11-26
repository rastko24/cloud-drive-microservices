package com.clouddrive.filesservice.controller;

import com.clouddrive.filesservice.dto.PromenaNazivaDto;
import com.clouddrive.filesservice.model.Datoteka;
import com.clouddrive.filesservice.service.DatotekaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.clouddrive.filesservice.security.OwnerAuthorization;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/fajlovi")
public class DatotekaController {
    private final DatotekaService datotekaService;
    private final OwnerAuthorization authorization;

    public DatotekaController(DatotekaService datotekaService, OwnerAuthorization authorization) {
        this.datotekaService = datotekaService;
        this.authorization = authorization;
    }

    @PostMapping(value = "/otpremi", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> otpremi(
            @RequestParam String vlasnikId,
            @RequestParam String folderId,
            @RequestParam("fajl") MultipartFile fajl, HttpServletRequest request) {
        authorization.requireOwner(vlasnikId, request);
        try {
            Datoteka datoteka = datotekaService.otpremi(vlasnikId, folderId, fajl);
            return ResponseEntity.status(HttpStatus.CREATED).body(datoteka);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }

    @PutMapping(value = "/{id}/verzija", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> novaVerzija(
            @PathVariable String id,
            @RequestParam String vlasnikId,
            @RequestParam("fajl") MultipartFile fajl, HttpServletRequest request) {
        authorization.requireOwner(vlasnikId, request);
        try {
            return ResponseEntity.ok(datotekaService.novaVerzija(id, vlasnikId, fajl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(e.getMessage());
        }
    }

    @GetMapping("/{id}/integritet")
    public ResponseEntity<?> integritet(@PathVariable String id, @RequestParam String vlasnikId,
                                        HttpServletRequest request) {
        authorization.requireOwner(vlasnikId, request);
        try { return ResponseEntity.ok(java.util.Map.of("ispravan",datotekaService.proveriIntegritet(id,vlasnikId))); }
        catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PutMapping("/{id}/naziv")
    public ResponseEntity<?> promeniNaziv(
            @PathVariable String id,
            @Valid @RequestBody PromenaNazivaDto dto, HttpServletRequest request) {
        authorization.requireOwner(dto.getVlasnikId(), request);
        try {
            return ResponseEntity.ok(datotekaService.preimenuj(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
