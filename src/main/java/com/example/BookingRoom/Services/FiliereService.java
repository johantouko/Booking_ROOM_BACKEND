package com.example.BookingRoom.Services;

import com.example.BookingRoom.Entities.Chambre;
import com.example.BookingRoom.Entities.Ecole;
import com.example.BookingRoom.Entities.Filiere;

import java.util.List;

public interface FiliereService {
    Filiere createFiliere(Filiere filiere);
    Filiere findbyId(long filiere_id);
    public int getTotalChambresByEcole(Long ecoleId);
    public boolean nomFiliereExists(String nom);
    List <Filiere> getAllFiliere();
    List<Filiere> getfilierebyecole(Ecole ecole);

}
