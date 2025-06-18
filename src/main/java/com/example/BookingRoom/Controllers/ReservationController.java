package com.example.BookingRoom.Controllers;

import com.example.BookingRoom.Entities.*;
import com.example.BookingRoom.Entities.DTO.ReservationRequestDTO;
import com.example.BookingRoom.Repository.ChambreRepository;
import com.example.BookingRoom.Repository.EcoleRepository;
import com.example.BookingRoom.Repository.FiliereRepository;
import com.example.BookingRoom.Repository.ReservationRepository;
import com.example.BookingRoom.ServiceImpl.SseService;
import com.example.BookingRoom.Services.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.*;
import jakarta.mail.Message;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lowagie.text.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.itextpdf.text.BaseColor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


import java.time.LocalDateTime;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    @PostMapping("/creer")
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

            // ✅ Vérification spécifique du nombre de chambres disponibles selon le sexe si sigle == "PV"
            Filiere filiere = reservation.getEtudiant().getFiliere();
//            Filiere filiereune = etudiant.getFiliere();
//            Ecole ecoleune = filiere.getEcole();
            Ecole ecole = filiere.getEcole();


            if ("PV".equalsIgnoreCase(ecole.getSigle())) {
                if ((etudiant.getSexe() == TypeSexeChambre.FEMININ && filiere.getNombreChambresFilledisponible() <= 0) || (etudiant.getSexe() == TypeSexeChambre.MASCULIN && filiere.getNombreChambresGarcondisponible() <= 0)) {

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
//                filiere.setNombreChambresDisponibles(filiere.getNombreChambresDisponibles() - 0.5);
                filiere.setNombreChambresDisponibles(Math.max(0, filiere.getNombreChambresDisponibles() - 0.5));


                // 8. Mettre à jour les compteurs dans l’école
//                ecole.setNombreChambresDisponibles(ecole.getNombreChambresDisponibles() - 0.5); // assure-toi que ce champ existe bien
                ecole.setNombreChambresDisponibles(Math.max(0, ecole.getNombreChambresDisponibles() - 0.5));


                if ("PV".equalsIgnoreCase(filiere.getEcole().getSigle())) {
                    if (etudiant.getSexe() == TypeSexeChambre.FEMININ) {
//                        filiere.setNombreChambresFilledisponible(filiere.getNombreChambresFilledisponible() - 0.5);
                        filiere.setNombreChambresFilledisponible(Math.max(0, filiere.getNombreChambresFilledisponible() - 0.5));


                    } else {
//                        filiere.setNombreChambresGarcondisponible(filiere.getNombreChambresGarcondisponible() - 0.5);
                        filiere.setNombreChambresGarcondisponible(Math.max(0, filiere.getNombreChambresGarcondisponible() - 0.5));

                    }
                }

                filiereService.updatefiliere(filiere);
                ecoleRepository.save(ecole);
                messagerieService.envoyerEmailEnAttente(etudiant , nouvellereservation.getDateFinReservation() );

                String message = "Liste-reservations";
                sseService.broadcastToAllUsers(reservationService.getAllReservations(),message);

                response.put("message", "Veuillez cliquer sur le bouton télécharger ci-dessous. \n Vous allez imprimer ce fichier et le joindre au reçu de paiement que vous déposerez à la comptabilité.");
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

            Reservation nouvellereservation = reservationService.updatereservation(reservation);
            boolean reservationcreer = (nouvellereservation != null);
            if (reservationcreer){
                // 7. Mettre à jour les compteurs dans la filière

               this.affecterEtudiantDepuisListeAttente(nouvellereservation);

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

            Chambre chambre = reservation.getChambre();
            System.out.println(chambre.getNumero() + "numero de la chambre");
            // 5. Mettre à jour l’emplacement demandé
            if (reservation.getEmplacementchambre().equalsIgnoreCase("Lit bas")) {
                chambre.setLitbas(StatutEmplacement.DISPONIBLE);
            } else if (reservation.getEmplacementchambre().equalsIgnoreCase("Lit mezzanine")) {
                chambre.setLitmezzanine(StatutEmplacement.DISPONIBLE);
            }

            chambre.setStatut(StatutChambre.LIBRE);
            System.out.println(chambre.getId());
            System.out.println(chambre.getStatut() + "chambre statut");
            chambreservice.majcahmabre(chambre);

            Filiere filiere = reservation.getEtudiant().getFiliere();
            filiere.setNombreChambresDisponibles(filiere.getNombreChambresDisponibles() + 0.5);
            // ✅ Mise à jour spécifique si l’école a le sigle PV
            if ("PV".equalsIgnoreCase(filiere.getEcole().getSigle())) {
                if (reservation.getEtudiant().getSexe() == TypeSexeChambre.FEMININ) {
                    filiere.setNombreChambresFilledisponible(filiere.getNombreChambresFilledisponible() + 0.5);
                } else if (reservation.getEtudiant().getSexe() == TypeSexeChambre.MASCULIN) {
                    filiere.setNombreChambresGarcondisponible(filiere.getNombreChambresGarcondisponible() + 0.5);
                }
            }
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

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> genererPdfStatistiques() {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, baos);
            document.open();

            // Chargement de la police (Montserrat ou défaut)
            Font fontTitre;
            Font fontCell;
            try {
                BaseFont baseFont = BaseFont.createFont("fonts/Montserrat-Regular.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                fontTitre = new Font(baseFont, 18, Font.BOLD);
                fontCell = new Font(baseFont, 12);
            } catch (Exception e) {
                fontTitre = new Font(Font.HELVETICA, 18, Font.BOLD);
                fontCell = new Font(Font.HELVETICA, 12);
            }

            // === PAGE DE GARDE ===
            try {
                Image logo = Image.getInstance(getClass().getClassLoader().getResource("static/logo.png"));
                logo.scaleToFit(400, 400);
                logo.setAlignment(Image.ALIGN_CENTER);
                for (int i = 0; i < 8; i++) document.add(new Paragraph(" "));
                document.add(logo);
            } catch (Exception e) {
                // logo non trouvé, on ignore
            }

            Paragraph titre = new Paragraph("Cité universitaire – Répartition des chambres 2025/2026", fontTitre);
            titre.setAlignment(Element.ALIGN_CENTER);
            titre.setSpacingBefore(30);
            document.add(titre);

            // === DONNÉES ===
            List<Chambre> chambres = chambreservice.getAllChambres();
            List<Reservation> reservations = reservationService.getreservationbystatut(StatutReservation.CONFIRMEE);

            // Groupement par niveau enum
            Map<NiveauChambre, List<Chambre>> chambresParNiveau = chambres.stream()
                    .sorted(Comparator.comparing(Chambre::getNumero))
                    .collect(Collectors.groupingBy(
                            Chambre::getNiveau,
                            () -> new TreeMap<>(Comparator.comparing(Enum::ordinal)),
                            Collectors.toList()
                    ));

            for (Map.Entry<NiveauChambre, List<Chambre>> entry : chambresParNiveau.entrySet()) {
                NiveauChambre niveau = entry.getKey();
                List<Chambre> chambresDuNiveau = entry.getValue();

                document.newPage();

                String nomNiveau = switch (niveau) {
                    case RDC -> "Rez-de-chaussée";
                    case Niveau1 -> "Niveau 1";
                    case Niveau2 -> "Niveau 2";
                    case Niveau3 -> "Niveau 3";
                };

                Paragraph titreNiveau = new Paragraph("Répartition – " + nomNiveau, fontTitre);
                titreNiveau.setAlignment(Element.ALIGN_LEFT);
                titreNiveau.setSpacingAfter(20);
                document.add(titreNiveau);

                PdfPTable table = new PdfPTable(4);
                table.setWidthPercentage(100);
                table.setWidths(new float[]{7f, 43f, 7f, 43f});
                table.setSpacingBefore(10f);

                int ligne = 0;

                for (int i = 0; i < chambresDuNiveau.size(); i += 2) {
                    Chambre ch1 = chambresDuNiveau.get(i);
                    Chambre ch2 = (i + 1 < chambresDuNiveau.size()) ? chambresDuNiveau.get(i + 1) : null;

                    PdfPCell cell1 = new PdfPCell(new Phrase(ch1.getNumero() + "", fontCell));
                    PdfPCell cell2 = new PdfPCell(buildContenuChambre(ch1, reservations, fontCell));
                    PdfPCell cell3 = new PdfPCell(new Phrase(ch2 != null ? ch2.getNumero() + "" : "", fontCell));
                    PdfPCell cell4 = new PdfPCell(ch2 != null ? buildContenuChambre(ch2, reservations, fontCell) : new Paragraph("", fontCell));

                    Stream.of(cell1, cell2, cell3, cell4).forEach(cell -> {
                        cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                        cell.setFixedHeight(55f);
                        cell.setPaddingLeft(8f);
                    });

                    if (ligne % 2 == 1) {
                        java.awt.Color gris = new java.awt.Color(230, 230, 230);
                        Stream.of(cell1, cell2, cell3, cell4).forEach(c -> c.setBackgroundColor(gris));
                    }

                    table.addCell(cell1);
                    table.addCell(cell2);
                    table.addCell(cell3);
                    table.addCell(cell4);
                    ligne++;
                }

                document.add(table);
            }

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", "repartition_chambres_complet.pdf");

            return ResponseEntity.ok().headers(headers).body(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private Paragraph buildContenuChambre(Chambre chambre, List<Reservation> reservations, Font font) {
        Paragraph contenu = new Paragraph();
        contenu.setFont(font);

        if (chambre.getTypechambre().equals(TypeChambre.INDIVIDUELLE)) {
            String nom = reservations.stream()
                    .filter(r -> r.getChambre().getId().equals(chambre.getId()))
                    .map(r -> "- " + r.getEtudiant().getNom() + " / " + r.getEtudiant().getWhatsappEtudiant())
                    .findFirst().orElse("- ");
            contenu.add(new Phrase(nom));
        } else {
            String lit1 = reservations.stream()
                    .filter(r -> r.getChambre().getId().equals(chambre.getId()) && "Lit bas".equalsIgnoreCase(r.getEmplacementchambre()))
                    .map(r -> "1 - " + r.getEtudiant().getNom() + " / " + r.getEtudiant().getWhatsappEtudiant())
                    .findFirst().orElse("1 -");

            String lit2 = reservations.stream()
                    .filter(r -> r.getChambre().getId().equals(chambre.getId()) && "Lit mezzanine".equalsIgnoreCase(r.getEmplacementchambre()))
                    .map(r -> "2 - " + r.getEtudiant().getNom() + " / " + r.getEtudiant().getWhatsappEtudiant())
                    .findFirst().orElse("2 -");

            contenu.add(new Phrase(lit1));
            contenu.add(Chunk.NEWLINE);
            contenu.add(Chunk.NEWLINE); // espacement entre lit 1 et lit 2
            contenu.add(new Phrase(lit2));
        }

        return contenu;
    }


    // === DÉTERMINATION DU NIVEAU ===
    private int determinerNiveau(String numeroChambre) {
        try {
            int num = Integer.parseInt(numeroChambre);
            if (num >= 0 && num <= 32) return 0;
            if (num >= 101 && num <= 132) return 1;
            if (num >= 201 && num <= 232) return 2;
            if (num >= 301 && num <= 332) return 3;
        } catch (NumberFormatException e) {
            return -1;
        }
        return -1;
    }


}
