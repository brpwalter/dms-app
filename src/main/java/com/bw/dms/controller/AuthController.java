package com.bw.dms.controller;

import com.bw.dms.dto.LoginRequest;
import com.bw.dms.model.User;
import com.bw.dms.repository.UserRepository;

import com.bw.dms.service.JwtService;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        // 1. Benutzer authentifizieren (wirft eine Exception bei falschen Daten)
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        // 2. Token-Generierung über den zentralen Service auslagern
        String token = jwtService.generateToken(request.getUsername());

        // 3. Token zurückgeben
        return ResponseEntity.ok(Map.of("token", token));
    }

    @PostMapping("/register")
public ResponseEntity<?> register(@RequestBody LoginRequest request) { // Falls dein DTO ein email-Feld hat
    if (userRepository.findByUsername(request.getUsername()).isPresent()) {
        return ResponseEntity.badRequest().body(Map.of("message", "Benutzername existiert bereits!"));
    }

    User newUser = new User();
    newUser.setUsername(request.getUsername());
    newUser.setPassword(passwordEncoder.encode(request.getPassword()));
    
    // HIER DIE E-MAIL ERGÄNZEN:
    newUser.setEmail(request.getEmail()); 

    userRepository.save(newUser);
    return ResponseEntity.ok(Map.of("message", "Benutzer erfolgreich registriert."));
}

}
