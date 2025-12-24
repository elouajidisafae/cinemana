package com.example.cinimana.model;

public enum StatutReservation {
    EN_ATTENTE, // Réservation créée, en attente de confirmation
    CONFIRMEE_CLIENT, // Client a confirmé sa présence par email
    VALIDEE, // Validée à la caisse (scannée)
    ANNULEE // Annulée (automatiquement ou manuellement)
}