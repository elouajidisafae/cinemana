package com.example.cinimana.dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ReservationRequest {
    private Long seanceId;
    private Integer nombrePlaces;
    private Long offreId;
    private List<SiegeRequest> sieges;
    // Snacks removed from reservation flow

}
