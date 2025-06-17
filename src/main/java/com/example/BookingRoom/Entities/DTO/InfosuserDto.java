package com.example.BookingRoom.Entities.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InfosuserDto {
    private String nom;
    private String email;
    private String emailutilisateurconnecter;
}
