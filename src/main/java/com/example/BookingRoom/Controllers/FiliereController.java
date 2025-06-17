package com.example.BookingRoom.Controllers;

import com.example.BookingRoom.Entities.DTO.FiliereDto;
import com.example.BookingRoom.Entities.Ecole;
import com.example.BookingRoom.Entities.Filiere;
import com.example.BookingRoom.Services.EcoleService;
import com.example.BookingRoom.Services.FiliereService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@RestController
@RequestMapping("/filieres")
@RequiredArgsConstructor
public class FiliereController {

    private final FiliereService filiereService;
    private final EcoleService ecoleService;

    // 🔹 Créer une nouvelle filière
    @PostMapping("")
    public Map<String, Object> createFiliere(@RequestBody FiliereDto request) {
        try {
            Map<String, Object> response = new HashMap<>();
            Filiere filiere = new Filiere();
            filiere.setNom(request.getNom());
            filiere.setNombreChambres(request.getNombreChambres());
            filiere.setNombreChambresGarcon(request.getNombreChambresGarcon());
            filiere.setNombreChambresFille(request.getNombreChambresFille());


            Ecole ecole = ecoleService.findbyId(request.getEcole());
            filiere.setEcole(ecole);

            if (filiereService.nomFiliereExists(filiere.getNom())){
                response.put("message", "Cette filière existe déjà.");
                response.put("success", false);
                return response;
            }

            if (ecole.getNombreChambres() < ((filiereService.getTotalChambresByEcole(filiere.getEcole().getId())) + filiere.getNombreChambres()) ){
                int nbrchambre = ecole.getNombreChambres() - filiereService.getTotalChambresByEcole(filiere.getEcole().getId());

                response.put("message", "Le nomnbre de chambre est supérieur au total de votre école. \n Il vous reste "+ nbrchambre  + " chambres" );
                response.put("success", false);
                return response;
            }

            if (Objects.equals(filiere.getEcole().getSigle(), "SJI") || Objects.equals(filiere.getEcole().getSigle(), "SJM")) {
                filiere.setNombreChambresFille(0);
                filiere.setNombreChambresGarcon(0);
            } else {
                if ((filiere.getNombreChambresFille() + filiere.getNombreChambresGarcon()) != filiere.getNombreChambres()) {
                    response.put("message", "Le nombre de chambres filles et garçons doit être égal au total du nombre de chambres de votre filière.");
                    response.put("success", false);
                    return response;
                }
            }

            Filiere nouvellefiliere = filiereService.createFiliere(filiere);
            boolean filierecreer = (nouvellefiliere != null);
            if (filierecreer){
                response.put("message", "Filière créée avec succès.");
                response.put("success", true);
                return response;
            }   else{
                response.put("success", false);
                response.put("message", "Enregistrement échoué. Une erreur est survenue");
            }
            return response;
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Enregistrement échoué. Une erreur est survenue");
            return response;
        }
    }

    @GetMapping("")
    public List<Filiere> getAllFfiliere() {
        return filiereService.getAllFiliere();
    }

    // 🔹 Lister les filières d'une école
    @GetMapping("/ecole")
    public List<Filiere> getFilieres(@RequestParam Long ecoleId) {
        return filiereService.getfilierebyecole(ecoleService.findbyId(ecoleId));
    }

    // 🔹 Lister les filières d'une école
    @GetMapping("/id")
    public Filiere getfilierebyid(@RequestParam Long id) {
        return filiereService.findbyId(id);
    }

}
