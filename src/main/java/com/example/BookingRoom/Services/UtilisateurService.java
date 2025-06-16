package com.example.BookingRoom.Services;

import com.example.BookingRoom.Entities.Utilisateur;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UtilisateurService {
    Utilisateur creer(Utilisateur utilisateur);
    void ajouterRole(String email, String nom_role);
    Utilisateur getUtilisateurByEmail(String email);
    List<Utilisateur> lister();
    Utilisateur getUtilisateurById(int id);
    Utilisateur modifier(Utilisateur utilisateur );
    boolean emailExists(String email);
    Utilisateur getConnectedUtilisateur();
    boolean aDejaLeRole(String email, String role);

}
