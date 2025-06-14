package com.example.BookingRoom.Entities.DTO;

import com.example.BookingRoom.Entities.TypeSexeChambre;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EtudiantRequestDTO {
    private Long id;
    private String nom;
    private int whatsappEtudiant;
    private int whatsappParent;
    private TypeSexeChambre sexe;
    private String email;
    private Long filiere;

}
