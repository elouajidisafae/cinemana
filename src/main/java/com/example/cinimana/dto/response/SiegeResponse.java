package com.example.cinimana.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SiegeResponse {
    private Integer rangee;
    private Integer numero;
    private Boolean disponible;
}
