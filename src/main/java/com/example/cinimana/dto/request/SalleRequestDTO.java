// src/main/java/com/example/cinimana/dto/request/SalleRequestDTO.java
package com.example.cinimana.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record SalleRequestDTO(
        @NotBlank String nom,
        @NotNull @Min(10) Integer capacite,
        @NotBlank String type,
        @NotNull @Min(1) Integer nombreRangees,
        @NotNull @Min(1) Integer siegesParRangee) {
}