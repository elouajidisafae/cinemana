package com.example.cinimana;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.cinimana.model.*;
import com.example.cinimana.repository.*;

@SpringBootApplication
@EnableScheduling
public class CinimanaApplication {

    public static void main(String[] args) {
        SpringApplication.run(CinimanaApplication.class, args);
    }

    @Bean
    CommandLineRunner initData(
            AdminRepository adminRepository,
            CommercialRepository commercialRepository,
            CaissierRepository caissierRepository,
            ClientRepository clientRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {

            // --- Admin ---
            if (adminRepository.findByEmail("admin@cinimana.com").isEmpty()) {
                Admin admin = new Admin();
                admin.setNom("Super");
                admin.setPrenom("Admin");
                admin.setEmail("admin@cinimana.com");
                admin.setMotDePasse(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                adminRepository.save(admin);
                System.out.println("Admin créé → email: admin@cinimana.com / mdp: admin123");
            }

        };
    }
}
