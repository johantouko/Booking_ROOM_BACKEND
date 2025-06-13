package com.example.BookingRoom.Repository;

import com.example.BookingRoom.Entities.Etudiant;
import com.example.BookingRoom.Entities.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EtudiantRepository extends JpaRepository<Etudiant, Long> {
    List<Etudiant> findByNomIsContainingIgnoreCase(String nom);
    List<Etudiant> findByFiliereOrderByNomAsc(Filiere filiere);

}
