package com.example.BookingRoom.Repository;

import com.example.BookingRoom.Entities.Etudiant;
import com.example.BookingRoom.Entities.Reservation;
import com.example.BookingRoom.Entities.StatutReservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsByEtudiantId(Long etudiantId);
    List<Reservation> findByStatut(StatutReservation statut);
    List<Reservation> findByEtudiant(Etudiant etudiant);

}
