package com.example.cinimana.service;

import com.example.cinimana.model.Admin;
import com.example.cinimana.repository.AdminRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AdminRepository adminRepository;

    public Admin getCurrentAdmin() {
        // Récupère l'email/username de l'utilisateur authentifié
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        // IMPORTANT : S'assurer que le repository pour Admin existe
        return adminRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Admin non trouvé dans le contexte de sécurité."));
    }
}