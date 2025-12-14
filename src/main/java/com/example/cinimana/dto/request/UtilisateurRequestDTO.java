package com.example.cinimana.dto.request;

import com.example.cinimana.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record UtilisateurRequestDTO(
        @NotBlank(message = "Le nom ne doit pas être vide") String nom,

        @NotBlank(message = "Le prénom ne doit pas être vide") String prenom,

        @NotBlank(message = "L'email ne doit pas être vide") @Email(message = "Le format de l'email est invalide") String email,

        String cin,
        LocalDate dateNaissance,
        LocalDate dateEmbauche,

        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères s'il est fourni.") String motDePasse,

        @NotNull(message = "Le rôle est obligatoire") Role role) {
}