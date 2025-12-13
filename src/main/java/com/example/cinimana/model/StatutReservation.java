package com.example.cinimana.model;

public enum StatutReservation {
    EN_ATTENTE("En attente"),
    VALIDEE("Validée"),
    ANNULEE("Annulée"),
    EXPIREE("Expirée");

    private final String libelle;

    StatutReservation(String libelle) {
        this.libelle = libelle;
    }

    public String getLibelle() {
        return libelle;
    }
}