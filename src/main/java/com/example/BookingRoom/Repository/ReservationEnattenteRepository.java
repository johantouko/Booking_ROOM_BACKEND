package com.example.BookingRoom.Repository;

import com.example.BookingRoom.Entities.ReservationEnattente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationEnattenteRepository extends JpaRepository<ReservationEnattente, Long> {
}
