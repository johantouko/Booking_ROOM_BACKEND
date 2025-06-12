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
public class Etudiant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name="nom")
    private String nom;

    private int whatsappEtudiant;
    private int whatsappParent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name="sexe")
    private TypeSexeChambre sexe;

    @Column(nullable = false, name="email")
    private String email;

    @ManyToOne
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;
}
