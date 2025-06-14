package com.example.BookingRoom.Controllers;


import com.example.BookingRoom.Entities.DTO.LoginResponseDTO;
import com.example.BookingRoom.Entities.User;
import com.example.BookingRoom.Repository.UserRepository;
import com.example.BookingRoom.Services.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public AuthController(AuthService authService,UserRepository userRepository) {
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> credentials) {
        String email = credentials.get("email");
        String password = credentials.get("password");
        System.out.println("Email: " + email);
        System.out.println("Password: " + password);
        Optional<User> userOpt = authService.login(email, password);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            LoginResponseDTO response = new LoginResponseDTO(user);
            return ResponseEntity.ok(response);
        }

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "Identifiants invalides"));
    }

    @PostMapping("/create-user")
    public ResponseEntity<?> createUser(@RequestBody Map<String, String> body) {
        String nom = body.get("nom");
        String email = body.get("email");
        String password = body.get("password");

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email déjà utilisé.");
        }

        User user = new User();
        user.setNom(nom);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFirstLogin(false);
        userRepository.save(user);
        return ResponseEntity.ok("Utilisateur créé avec succès.");
    }
}
