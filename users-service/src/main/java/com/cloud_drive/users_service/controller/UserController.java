package com.cloud_drive.users_service.controller;

import com.cloud_drive.users_service.dto.*;
import com.cloud_drive.users_service.model.User;
import com.cloud_drive.users_service.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        try {
            User registeredUser = userService.registerUser(registrationDto);
            
            UserResponseDto responseDto = new UserResponseDto(
                registeredUser.getId(),
                registeredUser.getEmail(),
                registeredUser.getIme(),
                registeredUser.getPrezime(),
                registeredUser.getKvotaSkladista(),
                registeredUser.getZauzetoSkladiste()
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }
}