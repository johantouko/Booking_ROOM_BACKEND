package com.example.BookingRoom.Entities;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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


public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, name="nom")
    private String nom;

    @Column(nullable = false, name="email")
    private String email;

    @Column(nullable = false, name="password")
    private String password;

    @Column(nullable = false, name="setFirstLogin")
    private Boolean firstLogin;

    public boolean isFirstLogin() {
        return this.firstLogin;
    }
}
