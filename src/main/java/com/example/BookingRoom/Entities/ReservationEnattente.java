package com.example.BookingRoom.Entities;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class ReservationEnattente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime dateReservation;

    private String emplacementchambre;

    @ManyToOne
    @JoinColumn(name = "etudiant_id")
    private Etudiant etudiant;

}
