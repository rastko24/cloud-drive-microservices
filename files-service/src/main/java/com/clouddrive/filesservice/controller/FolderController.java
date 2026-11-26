package com.clouddrive.filesservice.controller;

import com.clouddrive.filesservice.dto.FolderCreateDto;
import com.clouddrive.filesservice.dto.PromenaNazivaDto;
import com.clouddrive.filesservice.model.Folder;
import com.clouddrive.filesservice.service.FolderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import com.clouddrive.filesservice.security.OwnerAuthorization;

@RestController
@RequestMapping("/api/folderi")
public class FolderController {

    private final FolderService folderService;
    private final OwnerAuthorization authorization;

    public FolderController(FolderService folderService, OwnerAuthorization authorization) {
        this.folderService = folderService;
        this.authorization = authorization;
    }

    @PostMapping
    public ResponseEntity<?> kreiraj(@Valid @RequestBody FolderCreateDto dto, HttpServletRequest request) {
        authorization.requireOwner(dto.getVlasnikId(), request);
        try {
            Folder folder = folderService.kreirajFolder(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body(folder);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/sadrzaj")
    public ResponseEntity<?> sadrzaj(
            @RequestParam String vlasnikId,
            @RequestParam(required = false) String roditeljId, HttpServletRequest request) {
        authorization.requireOwner(vlasnikId, request);
        try {
            return ResponseEntity.ok(folderService.dobaviKompletanSadrzaj(vlasnikId, roditeljId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}/naziv")
    public ResponseEntity<?> promeniNaziv(
            @PathVariable String id,
            @Valid @RequestBody PromenaNazivaDto dto, HttpServletRequest request) {
        authorization.requireOwner(dto.getVlasnikId(), request);
        try {
            return ResponseEntity.ok(folderService.preimenuj(id, dto));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}