package com.example.BookingRoom.Entities.DTO;

import com.example.BookingRoom.Entities.Etudiant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRequestDTO {
    private String emplacementchambre;
    private long idchambre;
    private EtudiantRequestDTO etudiant;
}
