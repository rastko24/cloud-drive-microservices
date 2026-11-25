package com.example.notifications;

import com.mongodb.client.MongoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/notifikacije")
@CrossOrigin(origins = "${app.cors.allowed-origins:http://localhost:4200}")
public class NotifikacijaController {

    private final MongoTemplate notifikacijeTemplate;
    private final String collectionName;

    public NotifikacijaController(
            MongoClient mongoClient,
            @Value("${app.mongodb.database:notifikacije_db}") String databaseName,
            @Value("${app.mongodb.collection:notifikacije}") String collectionName
    ) {
        this.notifikacijeTemplate = new MongoTemplate(
                new SimpleMongoClientDatabaseFactory(mongoClient, databaseName)
        );
        this.collectionName = collectionName;
    }

    @GetMapping
    public List<Notifikacija> dobaviNotifikacije(@RequestParam String email) {
        Query query = new Query(Criteria.where("email").is(email.trim()))
                .with(Sort.by(Sort.Direction.DESC, "vremeKreiranja"));
        return notifikacijeTemplate.find(query, Notifikacija.class, collectionName);
    }
}