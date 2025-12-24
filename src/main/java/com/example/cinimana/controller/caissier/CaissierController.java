package com.example.cinimana.controller.caissier;

import com.example.cinimana.service.caissier.CaissierService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/caissier")
@PreAuthorize("hasRole('CAISSIER')")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CaissierController {

    private final CaissierService caissierService;

    @PostMapping("/verifier")
    public ResponseEntity<Map<String, Object>> verifierBillet(@RequestBody Map<String, String> body) {
        String code = body.get("code");
        Map<String, Object> result = caissierService.verifierBillet(code);

        if (Boolean.FALSE.equals(result.get("success"))) {
            if ("NOT_FOUND".equals(result.get("errorType"))) {
                return ResponseEntity.status(404).body(result);
            }
            return ResponseEntity.status(400).body(result);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/valider/{reservationId}")
    public ResponseEntity<Map<String, Object>> validerEntree(
            @PathVariable Long reservationId,
            Authentication authentication) {

        String email = (authentication != null) ? authentication.getName() : null;
        Map<String, Object> result = caissierService.validerEntree(reservationId, email);

        if (Boolean.FALSE.equals(result.get("success"))) {
            if ("❌ Réservation introuvable".equals(result.get("message"))) {
                return ResponseEntity.status(404).body(result);
            }
            return ResponseEntity.status(400).body(result);
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/annuler/{reservationId}")
    public ResponseEntity<Map<String, Object>> annulerEntree(
            @PathVariable Long reservationId,
            Authentication authentication) {

        String email = (authentication != null) ? authentication.getName() : null;
        Map<String, Object> result = caissierService.annulerEntree(reservationId, email);

        if (Boolean.FALSE.equals(result.get("success"))) {
            if ("❌ Réservation introuvable".equals(result.get("message"))) {
                return ResponseEntity.status(404).body(result);
            }
            return ResponseEntity.status(400).body(result);
        }

        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats(Authentication authentication) {
        String email = (authentication != null) ? authentication.getName() : null;
        return ResponseEntity.ok(caissierService.getStats(email));
    }
}
