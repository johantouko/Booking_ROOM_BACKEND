package com.example.BookingRoom.Repository;

import com.example.BookingRoom.Entities.Chambre;
import com.example.BookingRoom.Entities.StatutChambre;
import com.example.BookingRoom.Entities.TypeChambre;
import com.example.BookingRoom.Entities.TypeSexeChambre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChambreRepository extends JpaRepository<Chambre, Long> {
    List<Chambre> findByStatutAndTypechambre(String statut , TypeChambre typechambre); // Si tu ajoutes un champ `statut` dans Chambre
    List<Chambre> findByTypesexeAndTypechambre(TypeSexeChambre sexe, TypeChambre typechambre);

}
