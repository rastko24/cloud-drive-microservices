
package com.cloud_drive.users_service.controller;

import com.cloud_drive.users_service.dto.UserResponseDto;
import com.cloud_drive.users_service.model.User;
import com.cloud_drive.users_service.repository.UserRepository;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserProfileController {

    private final UserRepository userRepository;

    public UserProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/me")
    public ResponseEntity<?> trenutniKorisnik(Authentication prijava) {

        if (prijava == null || !prijava.isAuthenticated()) {
            return ResponseEntity
                    .status(HttpStatus.UNAUTHORIZED)
                    .body("Korisnik nije prijavljen.");
        }

        User korisnik = userRepository
                .findByEmail(prijava.getName())
                .orElse(null);

        if (korisnik == null) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body("Korisnik nije pronađen.");
        }

        UserResponseDto odgovor = new UserResponseDto(
                korisnik.getId(),
                korisnik.getEmail(),
                korisnik.getIme(),
                korisnik.getPrezime(),
                korisnik.getKvotaSkladista(),
                korisnik.getZauzetoSkladiste()
        );

        return ResponseEntity.ok(odgovor);
    }
}
