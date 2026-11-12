package com.clouddrive.filesservice.controller;

import com.clouddrive.filesservice.dto.PromenaNazivaDto;
import com.clouddrive.filesservice.service.DatotekaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/fajlovi")
@CrossOrigin(origins = "http://localhost:4200")
public class DatotekaController {

    private final DatotekaService datotekaService;

    public DatotekaController(DatotekaService datotekaService) {
        this.datotekaService = datotekaService;
    }

    @PutMapping("/{id}/naziv")
    public ResponseEntity<?> promeniNaziv(
            @PathVariable String id,
            @Valid @RequestBody PromenaNazivaDto dto) {
        try {
            return ResponseEntity.ok(datotekaService.preimenuj(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}