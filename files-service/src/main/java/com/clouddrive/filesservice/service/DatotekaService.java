package com.clouddrive.filesservice.service;

import com.clouddrive.filesservice.dto.PromenaNazivaDto;
import com.clouddrive.filesservice.model.Datoteka;
import com.clouddrive.filesservice.repository.DatotekaRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Service
public class DatotekaService {

    private final DatotekaRepository datotekaRepository;

    public DatotekaService(DatotekaRepository datotekaRepository) {
        this.datotekaRepository = datotekaRepository;
    }

    public List<Datoteka> datotekeUFolderu(String vlasnikId, String folderId) {
        if (folderId == null || folderId.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return datotekaRepository.findByVlasnikIdAndFolderId(vlasnikId, folderId);
    }

    public Datoteka preimenuj(String id, PromenaNazivaDto dto) {
        String naziv = dto.getNaziv().trim();
        proveriNaziv(naziv);
        String vlasnikId = dto.getVlasnikId().trim();

        Datoteka datoteka = datotekaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Datoteka nije pronađena!"));
        if (!vlasnikId.equals(datoteka.getVlasnikId())) {
            throw new RuntimeException("Nemate pristup ovoj datoteci!");
        }
        if (datotekaRepository.existsByVlasnikIdAndFolderIdAndNazivAndIdNot(
                vlasnikId, datoteka.getFolderId(), naziv, id)) {
            throw new RuntimeException("Datoteka sa tim nazivom već postoji u folderu!");
        }

        datoteka.setNaziv(naziv);
        datoteka.setVremeIzmene(LocalDateTime.now());
        return datotekaRepository.save(datoteka);
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