package com.example.BookingRoom.ServiceImpl;

import com.example.BookingRoom.Entities.*;
import com.example.BookingRoom.Repository.ChambreRepository;
import com.example.BookingRoom.Repository.ReservationEnattenteRepository;
import com.example.BookingRoom.Repository.ReservationRepository;
import com.example.BookingRoom.Repository.UserRepository;
import com.example.BookingRoom.Services.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
@EnableScheduling
@Service
    public class ReservationServiceImpl implements ReservationService {

        private final ReservationRepository reservationRepository;
        private final ReservationEnattenteRepository reservationEnattenteRepository;
        private final ChambreRepository chambreRepository;
        private final EcoleService ecoleService;
        private final FiliereService filiereservice;
        private final MessagerieService messagerieService;
        private final EtudiantService etudiantservice;
        private final UserRepository userRepository;

        public ReservationServiceImpl(ReservationRepository reservationRepository, ReservationEnattenteRepository reservationEnattenteRepository, ChambreRepository chambreRepository, EcoleService ecoleService, FiliereService filiereservice, MessagerieService messagerieService, EtudiantService etudiantservice, UserRepository userRepository) {
            this.reservationRepository = reservationRepository;
            this.reservationEnattenteRepository = reservationEnattenteRepository;
            this.chambreRepository = chambreRepository;
            this.ecoleService = ecoleService;
            this.messagerieService = messagerieService;
            this.etudiantservice = etudiantservice;
            this.filiereservice = filiereservice;
            this.userRepository = userRepository;
        }

        @Override
        public List<Reservation> getAllReservations() {
            return reservationRepository.findAll();
        }


    @Override
    public List<ReservationEnattente> getAllReservationsenattente() {
        return reservationEnattenteRepository.findAllByOrderByDateReservationAsc();
    }

    @Override
    public ReservationEnattente getFirstReservationsenattente() {
        return reservationEnattenteRepository.findTopByOrderByDateReservationAsc();
    }

    @Override
    public boolean supprimerReservationEnAttente(Long id) {
        Optional<ReservationEnattente> reservation = reservationEnattenteRepository.findById(id);
        if (reservation.isPresent()) {
            reservationEnattenteRepository.deleteById(id);
            return true;
        }
        return false;
    }


    @Override
    public List<Reservation> getreservationbyetudiant(Etudiant etudiant) {
        return this.reservationRepository.findByEtudiant(etudiant);
    }

    @Override
        public List<Reservation> getreservationbystatut(StatutReservation statut) {
            return reservationRepository.findByStatut(statut);
        }

        @Override
        public Reservation createReservation(Reservation reservation) {

            if (reservation.getEmplacementchambre().equalsIgnoreCase("Lit bas")) {
                reservation.getChambre().setLitbas(StatutEmplacement.RESERVER);
            } else if (reservation.getEmplacementchambre().equalsIgnoreCase("Lit mezzanine")) {
                reservation.getChambre().setLitmezzanine(StatutEmplacement.RESERVER);
            }
            if  ((reservation.getChambre().getLitbas() == StatutEmplacement.RESERVER) && (reservation.getChambre().getLitmezzanine() == StatutEmplacement.RESERVER) ) {
                reservation.getChambre().setStatut(StatutChambre.RESERVER);
            }

            if (reservation.getChambre().getLitbas() != StatutEmplacement.DISPONIBLE &&
                    reservation.getChambre().getLitmezzanine() != StatutEmplacement.DISPONIBLE) {
                reservation.getChambre().setStatut(StatutChambre.RESERVER);
            }
            reservation.setDateReservation(LocalDateTime.now());
            reservation.setDateFinReservation(this.getdatefinreservation(reservation.getDateReservation()));
            chambreRepository.save(reservation.getChambre());
            return reservationRepository.save(reservation);

        }

    @Override
    public Reservation updatereservation(Reservation reservation) {

        return reservationRepository.save(reservation);
    }

        @Override
        public boolean verifierReservationEtudiant(Etudiant etudiant) {
            return reservationRepository.existsByEtudiantId(etudiant.getId());
        }

    @Override
    public boolean verifierReservationEtudiantListeAttente(Etudiant etudiant) {
        return  reservationEnattenteRepository.existsByEtudiantId(etudiant.getId());
    }



    @Override
        public Reservation findById(Long id) {
            return reservationRepository.findById(id).orElse(null);
        }

        @Override
        public List<Map<String, Object>> getStatsChambresLibres() {
            List<Ecole> ecoles = ecoleService.getAllEcoles();
            List<Map<String, Object>> resultat = new ArrayList<>();

            for (Ecole ecole : ecoles) {
                Map<String, Object> ligne = new HashMap<>();
                ligne.put("ecole", ecole.getNom());
                ligne.put("totalChambres", ecole.getNombreChambres());
                ligne.put("chambresLibres", ecole.getNombreChambresDisponibles());
                resultat.add(ligne);
            }

            return resultat;
        }

         @Override
         public List<Map<String, Object>> getStatsChambresOccupees() {
             List<Ecole> ecoles = ecoleService.getAllEcoles();
             List<Map<String, Object>> resultat = new ArrayList<>();

             for (Ecole ecole : ecoles) {
                 List<Filiere> filieres = filiereservice.getfilierebyecole(ecole);

                 List<Etudiant> etudiants = filieres.stream()
                         .flatMap(f -> etudiantservice.getEtudaintByFiliere(f).stream())
                         .toList();

                 List<Reservation> reservations = etudiants.stream()
                         .flatMap(e -> this.getreservationbyetudiant(e).stream())
                         .toList();

                 Set<Chambre> chambres = reservations.stream()
                         .map(Reservation::getChambre)
                         .collect(Collectors.toSet());



                 long occupees = chambres.stream()
                         .filter(c -> c.getStatut() == StatutChambre.OCCUPE)
                         .count();

                 Map<String, Object> ligne = new HashMap<>();
                 ligne.put("ecole", ecole.getNom());
                 ligne.put("totalChambres", ecole.getNombreChambres());
                 ligne.put("chambresOccupees", occupees);
                 resultat.add(ligne);
             }

             return resultat;
         }

         @Override
         public List<Map<String, Object>> getStatsReservations() {
             List<Ecole> ecoles = ecoleService.getAllEcoles();
             List<Map<String, Object>> resultat = new ArrayList<>();

             for (Ecole ecole : ecoles) {
                 List<Filiere> filieres = filiereservice.getfilierebyecole(ecole);

                 List<Etudiant> etudiants = filieres.stream()
                         .flatMap(f -> etudiantservice.getEtudaintByFiliere(f).stream())
                         .toList();

                 List<Reservation> reservations = etudiants.stream()
                         .flatMap(e -> this.getreservationbyetudiant(e).stream())
                         .toList();

                 long confirmees = reservations.stream()
                         .filter(r -> r.getStatut() == StatutReservation.CONFIRMEE)
                         .count();

                 Map<String, Object> ligne = new HashMap<>();
                 ligne.put("ecole", ecole.getNom());
                 ligne.put("totalReservations", reservations.size());
                 ligne.put("reservationsConfirmees", confirmees);
                 resultat.add(ligne);
             }

             return resultat;
         }

    @Override
    public ReservationEnattente createreservationenattente(ReservationEnattente reservation) {
        return this.reservationEnattenteRepository.save(reservation);
    }


    //foonction qui envoie les mails chaque 12H pour l'échéance de la souscription
    @Scheduled(cron = "0 0 0,12 * * *")
    public void verifierReservationsEnEcheance() {
        System.out.println("je mexecute chaque 12heures");
        List<Reservation> reservationsEnAttente = this.getreservationbystatut(StatutReservation.EN_ATTENTE);

        for (Reservation reservation : reservationsEnAttente) {
            LocalDateTime dateLimite = reservation.getDateFinReservation();
            LocalDateTime seuilEcheance = dateLimite.minusHours(12);
            LocalDateTime maintenant = LocalDateTime.now();

            // Si on est au-delà du seuil de rappel
            if (maintenant.isAfter(seuilEcheance) && maintenant.isBefore(dateLimite)) {
                messagerieService.envoyerEmailEcheance(reservation); // À toi de définir cette méthode
            }
        }
    }

    //foonction qui envoie les mails chaque 05H pour l'échéance de l'annulation
    @Scheduled(cron = "0 0 0 * * *")
    public void checkannulationcreservation() {
        System.out.println("je mexecute chaque minuit");
        List<Reservation> reservationsEnAttente = this.getreservationbystatut(StatutReservation.EN_ATTENTE);
        List<User> users = this.userRepository.findAll();
        for (Reservation reservation : reservationsEnAttente) {
            LocalDateTime dateLimite = reservation.getDateFinReservation();
            LocalDateTime maintenant = LocalDateTime.now();

            // Si on est au-delà du seuil de rappel
            if (maintenant.isAfter(dateLimite))  {
                for (User user : users) {
                    messagerieService.envoyermailechenceannulation(user, reservation); // À toi de définir cette méthode
                }
            }
        }
    }

    /**
     * Calcule une date de fin en ajoutant 48 heures ouvrées à une date donnée.
     * - Les week-ends (samedi et dimanche) ne sont pas comptés dans les 48 heures.
     * - Si la date de départ est un samedi ou dimanche, le calcul commence le lundi suivant à la même heure.
     *

     */
    @Override
    public  LocalDateTime getdatefinreservation(LocalDateTime dateDepart) {
        // Si la date est un samedi ou un dimanche, on commence le lundi à la même heure
        if (dateDepart.getDayOfWeek() == DayOfWeek.SATURDAY) {
            dateDepart = dateDepart.plusDays(2); // Lundi
        } else if (dateDepart.getDayOfWeek() == DayOfWeek.SUNDAY) {
            dateDepart = dateDepart.plusDays(1); // Lundi
        }

        int heuresAjoutees = 0;
        LocalDateTime dateResultat = dateDepart;

        while (heuresAjoutees < 48) {
            dateResultat = dateResultat.plusHours(1);
            DayOfWeek jour = dateResultat.getDayOfWeek();

            if (jour != DayOfWeek.SATURDAY && jour != DayOfWeek.SUNDAY) {
                heuresAjoutees++;
            }
        }

        return dateResultat;
    }

}

