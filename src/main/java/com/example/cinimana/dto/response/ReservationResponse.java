package com.example.cinimana.dto.response;

import com.example.cinimana.model.StatutReservation;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private String codeReservation;
    private LocalDateTime dateReservation;
    private StatutReservation statut;
    private Double montantTotal;
    private Integer nombrePlace;
    private String ticketPdfUrl;

    // Info Seance
    private String filmTitre;
    private String filmAfficheUrl;
    private LocalDateTime seanceDateHeure;
    private String salleNom;

    // Info Sièges
    private List<SiegeInfo> sieges;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SiegeInfo {
        private Integer rangee;
        private Integer numero;
    }
}
