package com.clouddrive.filesservice.repository;

import com.clouddrive.filesservice.model.Datoteka;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface DatotekaRepository extends MongoRepository<Datoteka, String> {
    List<Datoteka> findByVlasnikIdAndFolderId(String vlasnikId, String folderId);
    boolean existsByVlasnikIdAndFolderIdAndNaziv(String vlasnikId, String folderId, String naziv);
    boolean existsByVlasnikIdAndFolderIdAndNazivAndIdNot(String vlasnikId, String folderId, String naziv, String id);
}