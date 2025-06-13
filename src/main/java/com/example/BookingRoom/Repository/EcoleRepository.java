package com.example.BookingRoom.Repository;

import com.example.BookingRoom.Entities.Ecole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EcoleRepository extends JpaRepository<Ecole, Long> {
    List<Ecole> findAllByOrderByNomAsc();

    // Pas besoin d'ajouter de méthodes custom ici pour l’instant
}
