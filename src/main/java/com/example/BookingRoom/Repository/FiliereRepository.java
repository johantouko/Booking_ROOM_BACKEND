package com.example.BookingRoom.Repository;

import com.example.BookingRoom.Entities.Ecole;
import com.example.BookingRoom.Entities.Filiere;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FiliereRepository extends JpaRepository<Filiere, Long> {
    List<Filiere> findByEcole_Id(long ecoleid);
    List<Filiere> findByEcole(Ecole ecole);
}
