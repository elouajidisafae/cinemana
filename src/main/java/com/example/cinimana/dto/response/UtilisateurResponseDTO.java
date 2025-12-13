// src/main/java/com/example/cinimana/dto/response/UtilisateurResponseDTO.java
package com.example.cinimana.dto.response;

import com.example.cinimana.model.Role;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

public record UtilisateurResponseDTO(
        String id,
        String nom,
        String prenom,
        String email,
        Role role,
        boolean actif,
        boolean premiereConnexion// ✅ CRUCIAL pour savoir si on doit proposer "Activer" ou "Désactiver"
) {}