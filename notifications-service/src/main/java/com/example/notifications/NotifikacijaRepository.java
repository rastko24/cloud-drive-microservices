package com.example.notifications;

import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;

public interface NotifikacijaRepository extends MongoRepository<Notifikacija, String> {

    @Query("{ 'email': { $regex: ?0, $options: 'i' } }")
    List<Notifikacija> pronadjiPoEmailu(String email, Sort sort);
}