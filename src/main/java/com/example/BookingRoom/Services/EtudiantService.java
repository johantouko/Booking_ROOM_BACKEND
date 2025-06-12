package com.example.BookingRoom.Services;

import com.example.BookingRoom.Entities.Etudiant;
import com.example.BookingRoom.Entities.Filiere;

import java.util.List;

public interface EtudiantService {
    Etudiant createEtudiant(Etudiant etudiant);
    Etudiant updateEtudiant(Long id, Etudiant etudiant);
    List <Etudiant> getAllEtudiants();
    List <Etudiant> getEtudaintByFiliere(Filiere filiere);
    List<Etudiant> searchEtudiantsByName(String q);
    Etudiant getetudiantbyId(long idetudiant);
    public boolean emailExists(String email);
    public boolean telephoneExists(int telephone);
    public boolean nameExists(String name);


}
