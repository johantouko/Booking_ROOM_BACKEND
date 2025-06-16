package com.example.BookingRoom.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    @Column(name="id_role")
    private int id_role;
    @Column(nullable = false, unique = true, name="nom")
    private String nom;


    public Role(int id_role, String nom) {
        this.id_role = id_role;
        this.nom = nom;
    }
}
