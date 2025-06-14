package com.example.BookingRoom.Entities.DTO;

import com.example.BookingRoom.Entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


public class LoginResponseDTO {
    private Long id;
    private String nom;
    private String prenom;
    private boolean firstLogin;

    public LoginResponseDTO(User user) {
        this.id = user.getId();
        this.nom = user.getNom();
        this.firstLogin = user.isFirstLogin();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }


    public boolean isFirstLogin() {
        return firstLogin;
    }

    public void setFirstLogin(boolean firstLogin) {
        this.firstLogin = firstLogin;
    }

}


