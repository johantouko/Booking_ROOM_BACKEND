package com.example.BookingRoom.Entities.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InfosconnexionDto {
    private String ancien;
    private String nouveau;
    private String emailutilisateurconnecter;
}
