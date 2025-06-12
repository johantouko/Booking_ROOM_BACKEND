package com.example.BookingRoom.Services;

import com.example.BookingRoom.Entities.Etudiant;
import com.example.BookingRoom.Entities.Reservation;

public interface MessagerieService {
    void envoyerEmailEnAttente(Etudiant etudiant);
    void envoyerEmailEcheance(Etudiant etudiant, String dateLimite);
    void envoyerEmailValidation(Reservation reservation);
}
