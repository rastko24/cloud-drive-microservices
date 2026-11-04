package com.clouddrive.filesservice.repository;

import com.clouddrive.filesservice.model.Folder;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FolderRepository extends MongoRepository<Folder, String> {
    // Pronađi foldere u korenu (gde je roditeljId null) za određenog vlasnika
    List<Folder> findByVlasnikIdAndRoditeljIdIsNull(String vlasnikId);

    // Pronađi podfoldere unutar određenog roditeljskog foldera
    List<Folder> findByVlasnikIdAndRoditeljId(String vlasnikId, String roditeljId);
}