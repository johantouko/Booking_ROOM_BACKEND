package com.example.BookingRoom.Repository;

import com.example.BookingRoom.Entities.Reservation;
import com.example.BookingRoom.Entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String emailUser);

}
