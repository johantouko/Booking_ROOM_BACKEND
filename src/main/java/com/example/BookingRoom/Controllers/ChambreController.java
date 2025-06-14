package com.example.BookingRoom.Controllers;

import com.example.BookingRoom.Entities.*;
import com.example.BookingRoom.Services.ChambreService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/chambres")
@RequiredArgsConstructor
public class ChambreController {

    private final ChambreService chambreService;

    @GetMapping("")
    public List<Chambre> getAllChambres() {
        return chambreService.getAllChambres();
    }


    @GetMapping("/sexe")
    public List<Chambre> getchambresbySexe(@RequestParam TypeSexeChambre sexe) {
        return chambreService.getchambrebysexe(sexe);
    }


    @PostMapping ("")
    public Map<String, Object> createChambre(@RequestBody Chambre chambre) {
        try {
            Map<String, Object> response = new HashMap<>();
            if (chambreService.numeroexists(chambre.getNumero())){
                response.put("message", "Cette chambre existe déjà.");
                response.put("success", false);
                return response;
            }

            Chambre nouvellechambre = chambreService.createChambre(chambre);
            boolean chambrecreer = (nouvellechambre != null);
            if (chambrecreer){
                response.put("message", "Chambre créée avec succès.");
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

    @PostMapping("/importall")
    public Map<String, Object> importchambre() {
        Map<String, Object> response = new HashMap<>();

        // Liste des numéros individuels
        Set<String> individuelles = Set.of(
                "026", "028", "032",
                "124", "126", "128", "130", "132",
                "204", "206", "208", "210", "212", "214", "218", "220", "222", "224", "226",
                "316", "326"
        );

        for (int niveau = 0; niveau < 4; niveau++) {
            for (int index = 1; index <= 32; index++) {
                Chambre chambre = new Chambre();

                // Numérotation (niveau 0 → 001 à 032, niveau 1 → 101 à 132, etc.)
                String numeroStr = String.format("%d%02d", niveau, index);
                int numero = Integer.parseInt(numeroStr);
                chambre.setNumero(numero);

                // Statuts initiaux
                chambre.setStatut(StatutChambre.LIBRE);
                chambre.setLitbas(StatutEmplacement.DISPONIBLE);
                chambre.setLitmezzanine(StatutEmplacement.DISPONIBLE);

                // Sexe de la chambre
                chambre.setTypesexe(
                        (niveau == 0 || niveau == 1) ? TypeSexeChambre.FEMININ : TypeSexeChambre.MASCULIN
                );

                // Type de chambre (individuelle ou double)
                if (individuelles.contains(numeroStr)) {
                    chambre.setTypechambre(TypeChambre.INDIVIDUELLE); // à créer si pas encore fait
                } else {
                    chambre.setTypechambre(TypeChambre.DOUBLE);
                }

                // Ajout du niveau
                chambre.setNiveau(switch (niveau) {
                    case 0 -> NiveauChambre.RDC;
                    case 1 -> NiveauChambre.Niveau1;
                    case 2 -> NiveauChambre.Niveau2;
                    case 3 -> NiveauChambre.Niveau3;
                    default -> throw new IllegalStateException("Niveau inconnu: " + niveau);
                });

            response = this.createChambre(chambre);
            }
        }
        return response;
    }



}
