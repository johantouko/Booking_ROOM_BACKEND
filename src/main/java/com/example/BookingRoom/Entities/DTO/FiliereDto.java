package com.example.BookingRoom.Entities.DTO;

import com.example.BookingRoom.Entities.TypeSexeChambre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FiliereDto {
    private String nom;
    private int nombreChambres;
    private int nombreChambresFille;
    private int nombreChambresGarcon;
    private Long ecole;
}
