package com.example.BookingRoom.Services;

import com.example.BookingRoom.Entities.Etudiant;
import com.example.BookingRoom.Entities.Reservation;

import java.time.LocalDateTime;

public interface MessagerieService {
    void envoyerEmailEnAttente(Etudiant etudiant , LocalDateTime localDateTime);
    void envoyerEmailEcheance(Reservation reservation);
    void envoyerEmailValidation(Reservation reservation);
    void envoyerEmailAnnulation(Reservation reservation);
}
