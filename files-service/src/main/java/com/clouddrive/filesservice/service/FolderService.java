package com.clouddrive.filesservice.service;

import com.clouddrive.filesservice.model.Folder;
import com.clouddrive.filesservice.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class FolderService {

    @Autowired
    private FolderRepository folderRepository;

    
    public Folder kreirajFolder(String naziv, String vlasnikId, String roditeljId) {
        
    	
        if (roditeljId != null && roditeljId.trim().isEmpty()) {
            roditeljId = null;
        }

        
        if (roditeljId != null) {
            boolean roditeljPostoji = folderRepository.existsById(roditeljId);
            if (!roditeljPostoji) {
                throw new RuntimeException("Roditeljski folder ne postoji!");
            }
        }

        Folder folder = new Folder();
        folder.setNaziv(naziv);
        folder.setVlasnikId(vlasnikId);
        folder.setRoditeljId(roditeljId);
        folder.setDatumKreiranja(LocalDateTime.now());

        return folderRepository.save(folder);
    }

    public List<Folder> dobaviFoldere(String vlasnikId, String roditeljId) {
        if (roditeljId == null || roditeljId.trim().isEmpty()) {
            return folderRepository.findByVlasnikIdAndRoditeljIdIsNull(vlasnikId);
        } else {
            return folderRepository.findByVlasnikIdAndRoditeljId(vlasnikId, roditeljId);
        }
    }
}