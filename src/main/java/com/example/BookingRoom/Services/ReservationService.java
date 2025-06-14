package com.example.BookingRoom.Services;

import com.example.BookingRoom.Entities.Etudiant;
import com.example.BookingRoom.Entities.Reservation;
import com.example.BookingRoom.Entities.ReservationEnattente;
import com.example.BookingRoom.Entities.StatutReservation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReservationService {
    List<Reservation> getAllReservations();
    List<Reservation> getreservationbyetudiant(Etudiant etudiant);
    List<Reservation> getreservationbystatut(StatutReservation statut);
    Reservation createReservation(Reservation reservation );
    Reservation updatereservation(Reservation reservation );
    public boolean verifierReservationEtudiant(Etudiant etudiant);
    public boolean verifierReservationEtudiantListeAttente(Etudiant etudiant);
    Reservation  findById(Long id);
    List<Map<String, Object>> getStatsChambresLibres();
    List<Map<String, Object>> getStatsChambresOccupees();
    List<Map<String, Object>> getStatsReservations();

    //Section liste des reservations en attente
    ReservationEnattente createreservationenattente(ReservationEnattente reservation );
    List<ReservationEnattente> getAllReservationsenattente();
    ReservationEnattente getFirstReservationsenattente();
    boolean supprimerReservationEnAttente(Long id);
    LocalDateTime getdatefinreservation(LocalDateTime localDateTime);


}
