// src/main/java/com/example/cinimana/dto/request/PasswordResetRequest.java
package com.example.cinimana.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(


        // Nouveau mot de passe
        @NotBlank(message = "Le nouveau mot de passe est requis.")
        @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères.")
        String newPassword
) { }