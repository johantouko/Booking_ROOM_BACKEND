package com.example.BookingRoom.Controllers;

import com.example.BookingRoom.Entities.DTO.EtudiantRequestDTO;
import com.example.BookingRoom.Entities.Etudiant;
import com.example.BookingRoom.Entities.Filiere;
import com.example.BookingRoom.Entities.Reservation;
import com.example.BookingRoom.Services.EtudiantService;
import com.example.BookingRoom.Services.FiliereService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/etudiants")
@RequiredArgsConstructor
public class EtudiantController {

    private final EtudiantService etudiantService;
    private final FiliereService filiereService;

    @GetMapping("")
    public List<Etudiant> getAllEtudiqnt() {

        return etudiantService.getAllEtudiants();
    }

    // 🔹 Créer un nouvel étudiant
    @PostMapping
    public Map<String, Object> createEtudiant(@RequestBody EtudiantRequestDTO request) {

        try {
            Etudiant etudiant = new Etudiant();
            etudiant.setNom(request.getNom());
            etudiant.setEmail(request.getEmail());
            etudiant.setWhatsappEtudiant(request.getWhatsappEtudiant());
            etudiant.setWhatsappParent(request.getWhatsappParent());
            etudiant.setSexe(request.getSexe());
            etudiant.setFiliere(filiereService.findbyId(request.getFiliere()));


            Map<String, Object> response = new HashMap<>();

            if (etudiantService.nameExists(etudiant.getNom())){
                response.put("message", "Ce nom existe déjà.");
                response.put("success", false);
                return response;
            }
            if (etudiantService.emailExists(etudiant.getEmail())){
                response.put("message", "Cet email est déja utilisé.");
                response.put("success", false);
                return response;
            }
            if (etudiantService.telephoneExists(etudiant.getWhatsappEtudiant())) {
                response.put("message", "Ce numero de telpehone est déja utilisé.");
                response.put("success", false);
                return response;
            }

            Etudiant nouvelEtudiant = etudiantService.createEtudiant(etudiant);
            boolean etudiantCree = (nouvelEtudiant != null);
            if (etudiantCree) {
                response.put("success", true);
                response.put("message", "Étudiant créé avec succès.");
            }
            else{
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

    // 🔹 Modifier les informations d’un étudiant
    @PutMapping("/{id}")
    public Etudiant updateEtudiant(@PathVariable Long id, @RequestBody Etudiant etudiant) {
        return etudiantService.updateEtudiant(id, etudiant);
    }

    @GetMapping("/searching")
    public List<Etudiant> getEtudiantByName(String q) {
        List<Etudiant> students = etudiantService.searchEtudiantsByName(q);
        return students ;
    }

    // 🔹 Lister les etudiant d'une filiere
    @GetMapping("/filiere")
    public List<Etudiant> getEtudiantbyfiliere(@RequestParam Long filiereid) {
        return etudiantService.getEtudaintByFiliere(filiereService.findbyId(filiereid));
    }

    // 🔹 Lister les etudiant d'une filiere
    @GetMapping("/id")
    public Etudiant getEtudiantbyid(@RequestParam Long idetudiant) {
        return etudiantService.getetudiantbyId(idetudiant);
    }


}
