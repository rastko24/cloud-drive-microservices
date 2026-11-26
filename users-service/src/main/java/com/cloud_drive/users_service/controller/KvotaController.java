package com.cloud_drive.users_service.controller;

import com.cloud_drive.users_service.model.User;
import com.cloud_drive.users_service.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@RestController
@RequestMapping("/api/internal/kvota")
public class KvotaController {

    private final UserRepository userRepository;
    private final MongoTemplate mongoTemplate;
    private final String interniToken;

    public KvotaController(
            UserRepository userRepository,
            MongoTemplate mongoTemplate,
            @Value("${internal.service.token:}") String interniToken) {

        this.userRepository = userRepository;
        this.mongoTemplate = mongoTemplate;
        this.interniToken = interniToken;
    }

    @PostMapping("/{id}/promeni")
    public ResponseEntity<String> promeniZauzece(
            @PathVariable String id,
            @RequestParam long promena,
            @RequestHeader(
                value = "X-Internal-Token",
                required = false
            ) String token) {

        if (interniToken.isBlank() || token == null ||
            !MessageDigest.isEqual(
                interniToken.getBytes(StandardCharsets.UTF_8),
                token.getBytes(StandardCharsets.UTF_8))) {

            return ResponseEntity
                    .status(HttpStatus.FORBIDDEN)
                    .body("Nedozvoljen interni poziv.");
        }

        for (int pokusaj = 0; pokusaj < 10; pokusaj++) {

            User user = userRepository.findById(id).orElse(null);

            if (user == null) {
                return ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body("Korisnik ne postoji.");
            }

            Long staroPolje = user.getZauzetoSkladiste();

            long staro = staroPolje == null
                    ? 0L : staroPolje;

            long kvota = user.getKvotaSkladista() == null
                    ? 0L : user.getKvotaSkladista();

            long novo;

            try {
                novo = Math.addExact(staro, promena);
            } catch (ArithmeticException e) {
                return ResponseEntity
                        .badRequest()
                        .body("Neispravna veličina datoteke.");
            }

            if (novo < 0 || novo > kvota) {
                return ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body("Nedovoljno prostora na nalogu.");
            }

            Query upit = Query.query(
                    Criteria.where("_id").is(id)
                            .and("zauzetoSkladiste").is(staroPolje)
            );

            Update izmena = new Update()
                    .set("zauzetoSkladiste", novo);

            User sacuvan = mongoTemplate.findAndModify(
                    upit,
                    izmena,
                    FindAndModifyOptions.options().returnNew(true),
                    User.class
            );

            if (sacuvan != null) {
                return ResponseEntity.ok("OK");
            }
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body("Paralelna izmena kvote; pokušajte ponovo.");
    }
}
