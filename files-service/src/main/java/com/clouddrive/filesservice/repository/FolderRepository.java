package com.clouddrive.filesservice.repository;

import com.clouddrive.filesservice.model.Folder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends MongoRepository<Folder, String> {
    List<Folder> findByVlasnikIdAndRoditeljIdIsNull(String vlasnikId);
    List<Folder> findByVlasnikIdAndRoditeljId(String vlasnikId, String roditeljId);
    
    boolean existsByVlasnikIdAndRoditeljIdIsNullAndNaziv(String vlasnikId, String naziv);
    boolean existsByVlasnikIdAndRoditeljIdAndNaziv(String vlasnikId, String roditeljId, String naziv);
    boolean existsByVlasnikIdAndRoditeljIdIsNullAndNazivAndIdNot(String vlasnikId, String naziv, String id);
    boolean existsByVlasnikIdAndRoditeljIdAndNazivAndIdNot(String vlasnikId, String roditeljId, String naziv, String id);
}