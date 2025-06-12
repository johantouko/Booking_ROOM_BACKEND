package com.example.BookingRoom.Controllers;

import com.example.BookingRoom.Entities.Chambre;
import com.example.BookingRoom.Entities.Ecole;
import com.example.BookingRoom.Entities.Filiere;
import com.example.BookingRoom.Entities.Reservation;
import com.example.BookingRoom.Services.EcoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/ecoles")
@RequiredArgsConstructor
public class EcoleController {

    private final EcoleService ecoleService;

    // 🔹 Créer une nouvelle école
    @PostMapping("")
    public Map<String, Object>  createEcole(@RequestBody Ecole ecole) {
        Map<String, Object> response = new HashMap<>();
        if (ecoleService.nameExists(ecole.getNom())){
            response.put("message", "Cette école existe déjà.");
            response.put("success", false);
            return response;
        }
        Ecole nouvelleEcole = ecoleService.createEcole(ecole);
        boolean ecolecreer = (nouvelleEcole != null);
        if (ecolecreer){
            response.put("message", "Ecole créée avec succès.");
            response.put("success", true);
            return response;
        }   else{
            response.put("success", false);
            response.put("message", "Enregistrement échoué . Une erreur est survenue");
        }
        return response;
    }

    @GetMapping("")
    public List<Ecole> getallecole() {
        return ecoleService.getAllEcoles();
    }


    // 🔹 Modifier une école existante
    @PutMapping("/{id}")
    public Ecole updateEcole(@PathVariable Long id, @RequestBody Ecole ecole) {
        return ecoleService.updateEcole(id, ecole);
    }






    // 🔹 Lister toutes les réservations des étudiants d'une école
//    @GetMapping("/{ecoleId}/reservations")
//    public List<Reservation> getReservations(@PathVariable Long ecoleId) {
//        return ecoleService.getReservationsByEcole(ecoleId);
//    }
}
