package com.example.BookingRoom.Controllers;

import com.example.BookingRoom.Entities.*;
import com.example.BookingRoom.Entities.DTO.ReservationRequestDTO;
import com.example.BookingRoom.Repository.ChambreRepository;
import com.example.BookingRoom.Repository.EcoleRepository;
import com.example.BookingRoom.Repository.FiliereRepository;
import com.example.BookingRoom.Repository.ReservationRepository;
import com.example.BookingRoom.ServiceImpl.SseService;
import com.example.BookingRoom.Services.*;
import jakarta.mail.Message;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
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
    private final ChambreService chambreservice;
    private final FiliereService filiereService;
    private final EcoleRepository ecoleRepository;
    private final EtudiantService etudiantService;
    private final MessagerieService messagerieService;
    private final SseService sseService;


    // 🔹 Lister toutes les réservations existantes
    @GetMapping(" ")
    public List<Reservation> getAllReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/sse/allReservation")
    public ResponseEntity<Void> getAllReservation() {
        String message = "Liste-reservations";
        sseService.broadcastToAllUsers(reservationService.getAllReservations(),message);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sse/allReservationEnAttente")
    public ResponseEntity<Void> getAllReservationEnAttente() {
        String message = "Liste-reservations-en-attente";
        sseService.broadcastToAllUsers(reservationService.getAllReservationsenattente(),message);
        return ResponseEntity.noContent().build();
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
    @GetMapping("/id")
    public Reservation getEtudiantbyid(@RequestParam Long idreservation) {
        return reservationService.findById(idreservation);
    }

    // 🔹 Créer une nouvelle réservation pour un étudiant
    @PostMapping(" ")
    public Map<String, Object> createReservation(@RequestBody ReservationRequestDTO request) {

        try {
            Map<String, Object> response = new HashMap<>();
            String emplacementchambre = request.getEmplacementchambre();
            Etudiant etudiant = new Etudiant();
            etudiant.setId(request .getEtudiant().getId());
            etudiant.setNom(request .getEtudiant().getNom());
            etudiant.setEmail(request .getEtudiant().getEmail());
            etudiant.setWhatsappEtudiant(request .getEtudiant().getWhatsappEtudiant());
            etudiant.setWhatsappParent(request .getEtudiant().getWhatsappParent());
            etudiant.setSexe(request .getEtudiant().getSexe());
            etudiant.setFiliere(filiereService.findbyId(request .getEtudiant().getFiliere()));

            Reservation reservation = new Reservation();
            reservation.setEmplacementchambre(emplacementchambre);
            reservation.setEtudiant(etudiant);
            reservation.setStatut(StatutReservation.EN_ATTENTE);


            if (reservationService.verifierReservationEtudiant(reservation.getEtudiant())) {
                response.put("message", "Cet étudiant a déjà effectué une réservation.");
                response.put("success", false);
                return response;
            }

            Etudiant etudiantExistant = etudiantService.getetudiantbyId(etudiant.getId());

            if (etudiantExistant != null) {
                // Mise à jour des informations
                etudiantExistant.setNom(etudiant.getNom());
                etudiantExistant.setWhatsappEtudiant(etudiant.getWhatsappEtudiant());
                etudiantExistant.setWhatsappParent(etudiant.getWhatsappParent());
                etudiantExistant.setSexe(etudiant.getSexe());
                etudiantExistant.setEmail(etudiant.getEmail());
                etudiant = etudiantService.createEtudiant(etudiantExistant);
                reservation.setEtudiant(etudiantExistant);

            } else {
                response.put("message", "Etudiant introuvable.");
                response.put("success", false);
                return response;
            }


            // ⚠ Vérifier le nombre de chambres disponibles
            if (etudiant.getFiliere().getNombreChambresDisponibles() <= 0) {
                // Créer une réservation en attente

                if (reservationService.verifierReservationEtudiantListeAttente(reservation.getEtudiant())) {
                    response.put("message", "Cet étudiant est déjà sur une liste d'attente.");
                    response.put("success", false);
                    return response;
                }

                ReservationEnattente attente = new ReservationEnattente();
                attente.setEtudiant(etudiant);
                attente.setDateReservation(LocalDateTime.now());

                reservationService.createreservationenattente(attente);  // tu dois avoir un service pour ça
                messagerieService.envoyerEmailListeAttente(attente);

                response.put("message", "Plus de chambres disponibles dans cette filière. L'étudiant a été placé sur liste d’attente.");
                response.put("success", true);
                return response;
            }

            Chambre chambre = chambreservice.getchambrebyid(request.getIdchambre());
            reservation.setChambre(chambre);


            if (chambre == null) {
                response.put("message", "Chambre introuvable.");
                response.put("success", false);
                return response;
            }


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


            if (emplacementchambre.equalsIgnoreCase("Lit bas")) {
                if (chambre.getLitbas() == StatutEmplacement.RESERVER) {
                    response.put("message", "Le lit bas de cette chambre est déjà réservé.");
                    response.put("success", false);
                    return response;
                }
            } else if (emplacementchambre.equalsIgnoreCase("Lit mezzanine")) {
                if (chambre.getLitmezzanine() == StatutEmplacement.RESERVER) {
                    response.put("message", "Le lit mezzanine de cette chambre est déjà réservé.");
                    response.put("success", false);
                    return response;}

            } else {
                response.put("message", "Emplacement invalide. Choisissez entre 'Lit bas' ou 'Lit mezzanine'.");
                response.put("success", false);
                return response;
            }

            Reservation nouvellereservation = reservationService.createReservation(reservation);
            boolean reservationcreer = (nouvellereservation != null);
            if (reservationcreer){
                // 7. Mettre à jour les compteurs dans la filière
                Filiere filiere = reservation.getEtudiant().getFiliere();
                filiere.setNombreChambresDisponibles(filiere.getNombreChambresDisponibles() - 0.5);

                // 8. Mettre à jour les compteurs dans l’école
                Ecole ecole = filiere.getEcole();
                ecole.setNombreChambresDisponibles(ecole.getNombreChambresDisponibles() - 0.5); // assure-toi que ce champ existe bien

                filiereService.updatefiliere(filiere);
                ecoleRepository.save(ecole);
                messagerieService.envoyerEmailEnAttente(etudiant , nouvellereservation.getDateFinReservation() );

                String message = "Liste-reservations";
                sseService.broadcastToAllUsers(reservationService.getAllReservations(),message);

                response.put("message", "Réservation créée avec succès.");
                response.put("success", true);
                return response;
            }   else{
                response.put("success", false);
                response.put("message", "Enregistrement échoué. Une erreur est survenue");
                return response;
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Enregistrement échoué. Une erreur est survenue");
            return response;
        }
    }

    @PostMapping("/validerReservation/{reservationId}")
    public Map<String, Object> accepterReservation( @PathVariable Long reservationId) {
        try {
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
            if (reservation.getStatut() != StatutReservation.EN_ATTENTE) {
                response.put("success", false);
                response.put("message", "Cette réservation a déjà confirmée ou annulée .");
                return response;
            }

            // 3. Changer le statut de la réservation
            reservation.setStatut(StatutReservation.CONFIRMEE);

            // 4. Récupérer la chambre
            Chambre chambre = reservation.getChambre();

            // 5. Mettre à jour l’emplacement demandé
            if (emplacement.equalsIgnoreCase("Lit bas")) {
                chambre.setLitbas(StatutEmplacement.OCCUPE);
            } else if (emplacement.equalsIgnoreCase("Lit mezzanine")) {
                chambre.setLitmezzanine(StatutEmplacement.OCCUPE);
            } else {
                response.put("success", false);
                response.put("message", "Emplacement invalide. Choisissez entre 'Lit bas' ou 'Lit mezzanine'.");
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
                chambreservice.majcahmabre(chambre);
                messagerieService.envoyerEmailValidation(nouvellereservation);
                String message = "Liste-reservations";
                sseService.broadcastToAllUsers(reservationService.getAllReservations(),message);
                sseService.broadcastToAllUsers(reservationService.getStatsChambresOccupees(),"Chambre-Occupees");
                sseService.broadcastToAllUsers(reservationService.getStatsReservations(),"Stat-Reservations");

                return response;
            }   else{
                response.put("success", false);
                response.put("message", "Enregistrement échoué. Une erreur est survenue");
                return response;

            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Enregistrement échoué. Une erreur est survenue");
            return response;
        }


    }

    @PostMapping("/annulerReservation/{reservationId}")
    public Map<String, Object> annulerReservation( @PathVariable Long reservationId) {
        try {
            Reservation reservation = reservationService.findById(reservationId);
            Map<String, Object> response = new HashMap<>();

            //etape 0
            if (LocalDateTime.now().isBefore(reservation.getDateFinReservation())) {
                response.put("message", "Les 48 heures accordées ne sont pas encore écoulées pour pouvoir annuler cette réservation.");
                response.put("success", false);
                return response;
            }

            // 1. Charger la réservation
            if (reservation == null) {
                response.put("message", "Réservation introuvable.");
                response.put("success", false);
                return response;
            }

            // 2. Vérifier si déjà confirmée
            if (reservation.getStatut() != StatutReservation.EN_ATTENTE) {
                response.put("success", false);
                response.put("message", "Cette réservation a déjà été confirmée ou annulée.");
                return response;
            }

            String emplacement = reservation.getEmplacementchambre();

            // 3. Changer le statut de la réservation
            reservation.setStatut(StatutReservation.REFUSEE);

            // 4. Récupérer la chambre
            Chambre chambre = reservation.getChambre();

            // 6. Si les deux emplacements sont Occupe  → chambre occupée
            if (chambre.getLitbas() == StatutEmplacement.DISPONIBLE &&
                    chambre.getLitmezzanine() == StatutEmplacement.DISPONIBLE) {
                chambre.setStatut(StatutChambre.LIBRE);
            }

            Reservation nouvellereservation = reservationService.updatereservation(reservation);
            boolean reservationcreer = (nouvellereservation != null);
            if (reservationcreer){
                // 7. Mettre à jour les compteurs dans la filière

               this.affecterEtudiantDepuisListeAttente(nouvellereservation);

                chambreservice.majcahmabre(chambre);
                messagerieService.envoyerEmailAnnulation(nouvellereservation);
                String message = "Liste-reservations";
                sseService.broadcastToAllUsers(reservationService.getAllReservations(),message);
                sseService.broadcastToAllUsers(reservationService.getStatsChambresLibres(),"Chambre-libres");
                sseService.broadcastToAllUsers(reservationService.getStatsReservations(),"Stat-Reservations");

                response.put("message", "Réservation anulée avec succès.");
                response.put("success", true);
                return response;
            }   else{
                response.put("success", false);
                response.put("message", "Enregistrement échoué. Une erreur est survenue");
                return response;
            }

        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", e.getMessage());
            return response;
        }

    }

    public void affecterEtudiantDepuisListeAttente(Reservation reservation) {
        System.out.println("reservation: " + reservation.getId());

        List<ReservationEnattente> listeAttente = reservationService.getAllReservationsenattente(); // déjà triée
        boolean etudiantAffecte = false;

        for (ReservationEnattente attente : listeAttente) {
            Etudiant etuAttente = attente.getEtudiant();
            Etudiant etuReservation = reservation.getEtudiant();

            boolean memeSexe = etuAttente.getSexe() == reservation.getChambre().getTypesexe();
            boolean memeFiliere = etuAttente.getFiliere().getId().equals(etuReservation.getFiliere().getId());

            if (memeSexe && memeFiliere) {
                Reservation newreservation=  new Reservation();
                newreservation.setEtudiant(etuAttente);
                newreservation.setEmplacementchambre(reservation.getEmplacementchambre());
                newreservation.setStatut(StatutReservation.EN_ATTENTE);
                newreservation.setChambre(reservation.getChambre());
                newreservation.setDateReservation(LocalDateTime.now());
                newreservation.setDateFinReservation(reservationService.getdatefinreservation(LocalDateTime.now()));
                reservationService.createReservation(newreservation);
                messagerieService.envoyerEmailEnAttente(etuAttente, newreservation.getDateFinReservation());
                reservationService.supprimerReservationEnAttente(attente.getId());

                etudiantAffecte = true;
                break;
            }
        }
        if (!etudiantAffecte) {

            // 5. Mettre à jour l’emplacement demandé
            if (reservation.getEmplacementchambre().equalsIgnoreCase("Lit bas")) {
                reservation.getChambre().setLitbas(StatutEmplacement.DISPONIBLE);
            } else if (reservation.getEmplacementchambre().equalsIgnoreCase("Lit mezzanine")) {
                reservation.getChambre().setLitmezzanine(StatutEmplacement.DISPONIBLE);
            }

            reservation.getChambre().setStatut(StatutChambre.LIBRE);
            chambreservice.majcahmabre(reservation.getChambre());

            Filiere filiere = reservation.getEtudiant().getFiliere();
            filiere.setNombreChambresDisponibles(filiere.getNombreChambresDisponibles() + 0.5);
            filiereService.updatefiliere(filiere);

            Ecole ecole = filiere.getEcole();
            ecole.setNombreChambresDisponibles(ecole.getNombreChambresDisponibles() + 0.5);
            ecoleRepository.save(ecole);
        }

        String message = "Liste-reservations";
        sseService.broadcastToAllUsers(reservationService.getAllReservations(),message);
        sseService.broadcastToAllUsers(reservationService.getStatsChambresOccupees(),"Chambre-Occupees");
        sseService.broadcastToAllUsers(reservationService.getStatsReservations(),"Stat-Reservations");
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

    @GetMapping("/sse/statchambres-libres")
    public ResponseEntity<Void> SsechambresLibres() {
        String message = "Chambre-libres";
        sseService.broadcastToAllUsers(reservationService.getStatsChambresLibres(),message);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sse/statchambres-occupees")
    public ResponseEntity<Void> SsechambresOccupees() {
        String message = "Chambre-Occupees";
        sseService.broadcastToAllUsers(reservationService.getStatsChambresOccupees(),message);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sse/statreservations")
    public ResponseEntity<Void> SsereservationsConfirmees() {
        String message = "Stat-Reservations";
        sseService.broadcastToAllUsers(reservationService.getStatsReservations(),message);
        return ResponseEntity.noContent().build();
    }

}
