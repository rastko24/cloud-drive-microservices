package com.clouddrive.filesservice.service;

import com.clouddrive.filesservice.dto.FolderCreateDto;
import com.clouddrive.filesservice.dto.PromenaNazivaDto;
import com.clouddrive.filesservice.dto.SadrzajFolderaDto;
import com.clouddrive.filesservice.model.Folder;
import com.clouddrive.filesservice.repository.FolderRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FolderService {

    private final FolderRepository folderRepository;
    private final DatotekaService datotekaService;

    public FolderService(FolderRepository folderRepository, DatotekaService datotekaService) {
        this.folderRepository = folderRepository;
        this.datotekaService = datotekaService;
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
        if (roditeljId == null) {
            if (folderRepository.existsByVlasnikIdAndRoditeljIdIsNullAndNaziv(vlasnikId, naziv)) {
                throw new RuntimeException("Folder sa tim nazivom već postoji!");
            }
        } else {
            if (folderRepository.existsByVlasnikIdAndRoditeljIdAndNaziv(vlasnikId, roditeljId, naziv)) {
                throw new RuntimeException("Folder sa tim nazivom već postoji!");
            }
        }
        Folder folder = new Folder(naziv, vlasnikId, roditeljId);
        return folderRepository.save(folder);
    }

    public SadrzajFolderaDto dobaviKompletanSadrzaj(String vlasnikId, String roditeljId) {
        List<Folder> folderi;
        if (roditeljId == null || roditeljId.trim().isEmpty()) {
            folderi = folderRepository.findByVlasnikIdAndRoditeljIdIsNull(vlasnikId);
            return new SadrzajFolderaDto(folderi, List.of());
        }
        Folder trenutni = folderRepository.findById(roditeljId)
            .orElseThrow(() -> new RuntimeException("Folder nije pronađen!"));
        if (!vlasnikId.equals(trenutni.getVlasnikId())) {
            throw new RuntimeException("Nemate pristup ovom folderu!");
        }
        folderi = folderRepository.findByVlasnikIdAndRoditeljId(vlasnikId, roditeljId);
        return new SadrzajFolderaDto(folderi, datotekaService.datotekeUFolderu(vlasnikId, roditeljId));
    }

    public Folder preimenuj(String id, PromenaNazivaDto dto) {
        String naziv = dto.getNaziv().trim();
        proveriNaziv(naziv);
        String vlasnikId = dto.getVlasnikId().trim();

        Folder folder = folderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Folder nije pronađen!"));
        if (!vlasnikId.equals(folder.getVlasnikId())) {
            throw new RuntimeException("Nemate pristup ovom folderu!");
        }
        String roditeljId = folder.getRoditeljId();
        if (roditeljId == null) {
            if (folderRepository.existsByVlasnikIdAndRoditeljIdIsNullAndNazivAndIdNot(vlasnikId, naziv, id)) {
                throw new RuntimeException("Folder sa tim nazivom već postoji!");
            }
        } else {
            if (folderRepository.existsByVlasnikIdAndRoditeljIdAndNazivAndIdNot(vlasnikId, roditeljId, naziv, id)) {
                throw new RuntimeException("Folder sa tim nazivom već postoji!");
            }
        }
        folder.setNaziv(naziv);
        return folderRepository.save(folder);
    }

    private void proveriNaziv(String naziv) {
        if (naziv.isEmpty()) {
            throw new RuntimeException("Naziv ne sme biti prazan!");
        }
        if (naziv.contains("/") || naziv.contains("\\") || naziv.contains("..")) {
            throw new RuntimeException("Naziv sadrži nedozvoljene karaktere!");
        }
    }
}