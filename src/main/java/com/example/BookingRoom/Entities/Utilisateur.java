package com.example.BookingRoom.Entities;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public class Utilisateur {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    @Column(name="id")
    private int id;

    @Column(nullable = false, name="nom")
    private String nom;


    @Column(nullable = false, unique = true, name="email")
    private String email;


    @Column(nullable = false, name="motDePasse" ,columnDefinition = "VARCHAR(255) DEFAULT 'changeme@2024'")
    private String motDePasse;

    @Column(name="token", columnDefinition = "VARCHAR(500)")
    private String token;

    @Column(nullable = false, name="default_password")
    protected boolean default_password;

    @ManyToMany(fetch = FetchType.EAGER)
    private Collection<Role> roles = new ArrayList<>();

    public String concatenateRoles(Collection<Role> roles) {
        return roles.stream().map(Role::getNom).collect(Collectors.joining(" "));
    }

    public Utilisateur(int id, String nom, String email, String motDePasse) {
        this.id = id;
        this.nom = nom;
        this.email = email;
        this.motDePasse = motDePasse;
    }
}
