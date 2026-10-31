package com.cloud_drive.users_service.service;

import com.cloud_drive.users_service.dto.UserRegistrationDto;
import com.cloud_drive.users_service.model.User;
import com.cloud_drive.users_service.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.findByEmail(registrationDto.getEmail()).isPresent()) {
            throw new RuntimeException("Korisnik sa ovim email-om već postoji!");
        }

        String hashedPassword = passwordEncoder.encode(registrationDto.getPassword());

        long defaultQuota = 5L * 1024 * 1024 * 1024; 

        User newUser = new User(
            registrationDto.getEmail(),
            hashedPassword,
            registrationDto.getFirstName(),
            registrationDto.getLastName(),
            defaultQuota
        );

        return userRepository.save(newUser);
    }
}