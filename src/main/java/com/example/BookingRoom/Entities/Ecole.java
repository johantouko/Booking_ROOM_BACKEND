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
public class Ecole {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name="nom")
    private String nom;

    @Column(nullable = false, name="sigle")
    private String sigle;

    private double nombreChambresDisponibles;

    @Column(nullable = false, name="nombreChambres")
    private int nombreChambres;

    @Column(nullable = false, name="nombreChambresIndividuelle")
    private int nombreChambresIndividuelle;

}
