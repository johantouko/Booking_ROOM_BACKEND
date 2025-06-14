package com.example.BookingRoom.Repository;

import com.example.BookingRoom.Entities.ReservationEnattente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationEnattenteRepository extends JpaRepository<ReservationEnattente, Long> {
    boolean existsByEtudiantId(Long etudiantId);
    List<ReservationEnattente> findAllByOrderByDateReservationAsc();
    ReservationEnattente findTopByOrderByDateReservationAsc();
}
