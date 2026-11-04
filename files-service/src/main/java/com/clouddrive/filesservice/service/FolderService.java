package com.clouddrive.filesservice.service;

import com.clouddrive.filesservice.dto.FolderCreateDto;
import com.clouddrive.filesservice.model.Folder;
import com.clouddrive.filesservice.repository.FolderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;

    public FolderService(FolderRepository folderRepository) {
        this.folderRepository = folderRepository;
    }

    public Folder kreirajFolder(FolderCreateDto dto) {
        String naziv = dto.getNaziv().trim();
        proveriNaziv(naziv);

        String vlasnikId = dto.getVlasnikId().trim();
        String roditeljId = dto.getRoditeljId();
        if (roditeljId != null && roditeljId.trim().isEmpty()) {
            roditeljId = null;
        }

        if (roditeljId != null) {
            Folder roditelj = folderRepository.findById(roditeljId)
                .orElseThrow(() -> new RuntimeException("Roditeljski folder ne postoji!"));
            if (!vlasnikId.equals(roditelj.getVlasnikId())) {
                throw new RuntimeException("Nemate pravo da kreirate folder ovde!");
            }
        }

        Folder folder = new Folder(naziv, vlasnikId, roditeljId);
        return folderRepository.save(folder);
    }

    public List<Folder> dobaviSadrzaj(String vlasnikId, String roditeljId) {
        if (roditeljId == null || roditeljId.trim().isEmpty()) {
            return folderRepository.findByVlasnikIdAndRoditeljIdIsNull(vlasnikId);
        }
        Folder trenutni = folderRepository.findById(roditeljId)
            .orElseThrow(() -> new RuntimeException("Folder nije pronađen!"));
        if (!vlasnikId.equals(trenutni.getVlasnikId())) {
            throw new RuntimeException("Nemate pristup ovom folderu!");
        }
        return folderRepository.findByVlasnikIdAndRoditeljId(vlasnikId, roditeljId);
    }

    private void proveriNaziv(String naziv) {
        if (naziv.isEmpty()) {
            throw new RuntimeException("Naziv foldera ne sme biti prazan!");
        }
        if (naziv.contains("/") || naziv.contains("\\") || naziv.contains("..")) {
            throw new RuntimeException("Naziv sadrži nedozvoljene karaktere!");
        }
    }
}