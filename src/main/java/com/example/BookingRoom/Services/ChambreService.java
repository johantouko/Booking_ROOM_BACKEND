package com.example.BookingRoom.Services;

import com.example.BookingRoom.Entities.Chambre;
import com.example.BookingRoom.Entities.TypeSexeChambre;

import java.util.List;

public interface ChambreService {
    List<Chambre> getAllChambres();
    List<Chambre> getchambrebysexe(TypeSexeChambre sexe);
    List<Chambre> getChambresByStatut(String statut);
    Chambre createChambre(Chambre chambre);
    Chambre getchambrebyid(long idchambre);
    public boolean numeroexists(int numerochambre);
    Chambre majcahmabre (Chambre chambre);

}
