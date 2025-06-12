package com.example.BookingRoom.Repository;

import com.example.BookingRoom.Entities.Chambre;
import com.example.BookingRoom.Entities.StatutChambre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChambreRepository extends JpaRepository<Chambre, Long> {
    List<Chambre> findByStatut(String statut); // Si tu ajoutes un champ `statut` dans Chambre

}
