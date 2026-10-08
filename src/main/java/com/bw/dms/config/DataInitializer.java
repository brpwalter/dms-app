package com.bw.dms.config;

import com.bw.dms.model.User;
import com.bw.dms.repository.UserRepository;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    DataInitializer(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

   @Override
public void run(String... args) throws Exception {
    // Wir suchen gezielt nach dem admin
    java.util.Optional<User> existingAdmin = userRepository.findByUsername("admin");
    
    if (existingAdmin.isPresent()) {
        // Falls er existiert, überschreiben wir das Passwort mit dem korrekten Bcrypt-Hash
        User admin = existingAdmin.get();
        admin.setPassword(passwordEncoder.encode("password123"));
        admin.setEmail("admin@example.com");
        userRepository.save(admin);
        System.out.println("--> [DMS] BESTEHENDER Admin-User wurde repariert (Passwort-Hash erneuert)!");
    } else {
        // Falls er noch gar nicht existiert, legen wir ihn neu an
        User admin = new User();
        admin.setUsername("admin");
        admin.setEmail("admin@example.com");
        admin.setPassword(passwordEncoder.encode("password123"));
        userRepository.save(admin);
        System.out.println("--> [DMS] NEUER Testbenutzer erfolgreich angelegt: admin / password123");
    }
}

}
