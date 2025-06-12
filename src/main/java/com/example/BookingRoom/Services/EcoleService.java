package com.example.BookingRoom.Services;

import com.example.BookingRoom.Entities.Chambre;
import com.example.BookingRoom.Entities.Ecole;
import com.example.BookingRoom.Entities.Filiere;
import com.example.BookingRoom.Entities.Reservation;

import java.util.List;

public interface EcoleService {
    Ecole createEcole(Ecole ecole);
    Ecole updateEcole(Long id, Ecole ecole);
    List <Ecole> getAllEcoles();
    Ecole findbyId(Long id);
//    List<Reservation> getReservationsByEcole(Long ecoleId);
    public boolean nameExists(String name);
}
