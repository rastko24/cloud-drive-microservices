package com.clouddrive.filesservice.controller;

import com.clouddrive.filesservice.dto.FolderCreateDto;
import com.clouddrive.filesservice.model.Folder;
import com.clouddrive.filesservice.service.FolderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/folderi")
@CrossOrigin(origins = "http://localhost:4200")
public class FolderController {

    private final FolderService folderService;

    public FolderController(FolderService folderService) {
        this.folderService = folderService;
    }

    @PostMapping
    public ResponseEntity<?> kreiraj(@Valid @RequestBody FolderCreateDto dto) {
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
            @RequestParam(required = false) String roditeljId) {
        try {
            List<Folder> lista = folderService.dobaviSadrzaj(vlasnikId, roditeljId);
            return ResponseEntity.ok(lista);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}