package com.example.BookingRoom.Controllers;

import com.example.BookingRoom.Entities.*;
import com.example.BookingRoom.Entities.DTO.ReservationRequestDTO;
import com.example.BookingRoom.Repository.ChambreRepository;
import com.example.BookingRoom.Repository.EcoleRepository;
import com.example.BookingRoom.Repository.FiliereRepository;
import com.example.BookingRoom.Repository.ReservationRepository;
import com.example.BookingRoom.Services.ChambreService;
import com.example.BookingRoom.Services.EtudiantService;
import com.example.BookingRoom.Services.MessagerieService;
import com.example.BookingRoom.Services.ReservationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private static final Logger log = LoggerFactory.getLogger(ReservationController.class);
    private final ReservationService reservationService;

    private final ReservationRepository reservationRepository;
    private final ChambreService chamreservice;
    private final FiliereRepository filiereRepository;
    private final EcoleRepository ecoleRepository;
    private final EtudiantService etudiantService;
    private final MessagerieService messagerieService;

    // 🔹 Lister toutes les réservations existantes
    @GetMapping(" ")
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }
    @GetMapping("/enattente")
    public List<Reservation> getreservationenattente() {
        return reservationService.getreservationbystatut(StatutReservation.EN_ATTENTE);
    }
    @GetMapping("/annuler")
    public List<Reservation> getreservationannuler() {
        return reservationService.getreservationbystatut(StatutReservation.REFUSEE);
    }
    @GetMapping("/accepter")
    public List<Reservation> getreservationaccepter() {
        return reservationService.getreservationbystatut(StatutReservation.CONFIRMEE);
    }

    // 🔹 Créer une nouvelle réservation pour un étudiant
    @PostMapping(" ")
    public Map<String, Object> createReservation(@RequestBody ReservationRequestDTO request) {
        Map<String, Object> response = new HashMap<>();
        String emplacementchambre = request.getEmplacementchambre();
        Chambre chambre = chamreservice.getchambrebyid(request.getIdchambre());
        Etudiant etudiant = etudiantService.getetudiantbyId(request.getIdEtudiant());

            if (chambre == null) {
                response.put("message", "Chambre introuvable.");
                response.put("success", false);
                return response;
            }
            if (etudiant == null) {
                response.put("message", "Etudiant introuvable.");
                response.put("success", false);
                return response;
            }
            Reservation reservation = new Reservation();
            reservation.setChambre(chambre);
            reservation.setEmplacementchambre(emplacementchambre);
            reservation.setEtudiant(etudiant);
            reservation.setStatut(StatutReservation.EN_ATTENTE);

            if (reservation.getChambre().getStatut() == StatutChambre.OCCUPE) {
                response.put("message", "cette chambre est déjà occupée.");
                response.put("success", false);
                return response;
            }

            if (reservation.getEtudiant().getSexe() != reservation.getChambre().getTypesexe()){
                response.put("message", "La chambre sélectionnée est réservée à un sexe différent du vôtre.");
                response.put("success", false);
                return response;
            }

            if (reservationService.verifierReservationEtudiant(reservation.getEtudiant())) {
                response.put("message", "Cet étudiant a déjà effectué une réservation.");
                response.put("success", false);
                return response;
            }


        if (emplacementchambre.equalsIgnoreCase("litbas")) {
            if (chambre.getLitbas() == StatutEmplacement.RESERVER) {
                response.put("message", "Le lit bas de cette chambre est déjà réservé.");
                response.put("success", false);
                return response;
            }
        } else if (emplacementchambre.equalsIgnoreCase("litmezzanine")) {
            if (chambre.getLitmezzanine() == StatutEmplacement.RESERVER) {
                response.put("message", "Le lit mezzanine de cette chambre est déjà réservé.");
                response.put("success", false);
                return response;}

        } else {
            response.put("message", "Emplacement invalide. Choisissez entre 'litbas' ou 'litmezzanine'.");
            response.put("success", false);
            return response;
        }

            Reservation nouvellereservation = reservationService.createReservation(reservation);
            boolean reservationcreer = (nouvellereservation != null);
            if (reservationcreer){
                // 7. Mettre à jour les compteurs dans la filière
                Filiere filiere = reservation.getEtudiant().getFiliere();
                filiere.setNombreChambresDisponibles(filiere.getNombreChambresDisponibles() - 1);

                // 8. Mettre à jour les compteurs dans l’école
                Ecole ecole = filiere.getEcole();
                ecole.setNombreChambresDisponibles(ecole.getNombreChambresDisponibles() - 1); // assure-toi que ce champ existe bien

                filiereRepository.save(filiere);
                ecoleRepository.save(ecole);
                System.out.println(nouvellereservation.getDateReservation().plusHours(48));
//                messagerieService.envoyerEmailEnAttente(etudiant);


                response.put("message", "Réservation créée avec succès.");
                response.put("success", true);
                return response;
            }   else{
                response.put("success", false);
                response.put("message", "Enregistrement échoué . Une erreur est survenue");
            }
            return response;
    }

    @PostMapping("/validerReservation")
    public Map<String, Object> accepterReservation( @RequestBody Long reservationId) {
        Map<String, Object> response = new HashMap<>();

        // 1. Charger la réservation
        Reservation reservation = reservationService.findById(reservationId);
        if (reservation == null) {
            response.put("message", "Réservation introuvable.");
            response.put("success", false);
            return response;
        }

        String emplacement = reservation.getEmplacementchambre();

        // 2. Vérifier si déjà confirmée
        if (reservation.getStatut() == StatutReservation.CONFIRMEE) {
            response.put("success", false);
            response.put("message", "Cette réservation est déjà confirmée.");
            return response;
        }

        // 3. Changer le statut de la réservation
        reservation.setStatut(StatutReservation.CONFIRMEE);

        // 4. Récupérer la chambre
        Chambre chambre = reservation.getChambre();

        // 5. Mettre à jour l’emplacement demandé
        if (emplacement.equalsIgnoreCase("litbas")) {
            chambre.setLitbas(StatutEmplacement.OCCUPE);
        } else if (emplacement.equalsIgnoreCase("litmezzanine")) {
            chambre.setLitmezzanine(StatutEmplacement.OCCUPE);
        } else {
            response.put("success", false);
            response.put("message", "Emplacement invalide. Choisissez entre 'litbas' ou 'litmezzanine'.");
            return response;
        }

        // 6. Si les deux emplacements sont Occupe  → chambre occupée
        if (chambre.getLitbas() == StatutEmplacement.OCCUPE &&
                chambre.getLitmezzanine() == StatutEmplacement.OCCUPE) {
            chambre.setStatut(StatutChambre.OCCUPE);
        }


        Reservation nouvellereservation = reservationRepository.save(reservation);
        boolean reservationcreer = (nouvellereservation != null);
        if (reservationcreer){
            // 7. Mettre à jour les compteurs dans la filière
            response.put("message", "Réservation confirmée avec succès.");
            response.put("success", true);
            chamreservice.majcahmabre(chambre);
            messagerieService.envoyerEmailValidation(nouvellereservation);
            return response;
        }   else{
            response.put("success", false);
            response.put("message", "Enregistrement échoué . Une erreur est survenue");
        }
        return response;

    }

    @GetMapping("/statchambres-libres")
    public List<Map<String, Object>> chambresLibres() {
        return reservationService.getStatsChambresLibres();
    }

    @GetMapping("/statchambres-occupees")
    public List<Map<String, Object>> chambresOccupees() {
        return reservationService.getStatsChambresOccupees();
    }

    @GetMapping("/statreservations")
    public List<Map<String, Object>> reservationsConfirmees() {
        return reservationService.getStatsReservations();
    }

}
