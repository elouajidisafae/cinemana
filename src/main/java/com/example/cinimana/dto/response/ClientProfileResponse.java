package com.example.cinimana.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class ClientProfileResponse {
    private String nom;
    private String prenom;
    private String email;
    private String numeroTelephone;
    private LocalDate dateNaissance;
}
