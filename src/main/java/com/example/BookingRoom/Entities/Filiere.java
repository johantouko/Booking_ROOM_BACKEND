package com.example.BookingRoom.Entities;

import jakarta.persistence.*;

import java.util.ArrayList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Filiere {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;
    private int nombreChambres;
    private double nombreChambresDisponibles;

    private double nombreChambresFilledisponible;
    private int nombreChambresFille;
    private int nombreChambresGarcon;
    private double nombreChambresGarcondisponible;




    @ManyToOne
    @JoinColumn(name = "ecole")
    private Ecole ecole;

}
