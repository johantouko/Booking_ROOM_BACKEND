package com.example.BookingRoom.Entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity

public class Chambre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('LIBRE', 'OCCUPE' , 'RESERVER') DEFAULT 'LIBRE'")
    private StatutChambre statut = StatutChambre.LIBRE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "ENUM('RESERVER', 'DISPONIBLE' , 'OCCUPE') DEFAULT 'DISPONIBLE'")
    private StatutEmplacement litbas = StatutEmplacement.DISPONIBLE;


    @Enumerated(EnumType.STRING)
    @Column(nullable = false,  columnDefinition = "ENUM('RESERVER', 'DISPONIBLE' , 'OCCUPE') DEFAULT 'DISPONIBLE'")
    private StatutEmplacement litmezzanine = StatutEmplacement.DISPONIBLE;

    @Column(nullable = false, name="typesexe")
    @Enumerated(EnumType.STRING)
    private TypeSexeChambre typesexe;

    @Column(nullable = false, name="niveau")
    @Enumerated(EnumType.STRING)
    private NiveauChambre niveau;

    @Column(nullable = false, name="typechambre")
    @Enumerated(EnumType.STRING)
    private TypeChambre typechambre;

}
