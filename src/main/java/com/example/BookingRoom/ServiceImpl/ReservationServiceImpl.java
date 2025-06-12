package com.example.BookingRoom.ServiceImpl;

import com.example.BookingRoom.Entities.*;
import com.example.BookingRoom.Repository.ChambreRepository;
import com.example.BookingRoom.Repository.ReservationRepository;
import com.example.BookingRoom.Services.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
    public class ReservationServiceImpl implements ReservationService {

        private final ReservationRepository reservationRepository;
        private final ChambreRepository chambreRepository;
        private final EcoleService ecoleService;
        private final FiliereService filiereservice;
        private final EtudiantService etudiantservice;

        public ReservationServiceImpl(ReservationRepository reservationRepository, ChambreRepository chambreRepository, EcoleService ecoleService, FiliereService filiereservice, EtudiantService etudiantservice) {
            this.reservationRepository = reservationRepository;
            this.chambreRepository = chambreRepository;
            this.ecoleService = ecoleService;
            this.etudiantservice = etudiantservice;
            this.filiereservice = filiereservice;
        }

        @Override
        public List<Reservation> getAllReservations() {
            return reservationRepository.findAll();
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

            if (reservation.getEmplacementchambre().equalsIgnoreCase("litbas")) {
                reservation.getChambre().setLitbas(StatutEmplacement.RESERVER);
            } else if (reservation.getEmplacementchambre().equalsIgnoreCase("litmezzanine")) {
                reservation.getChambre().setLitmezzanine(StatutEmplacement.RESERVER);
            }
            if  ((reservation.getChambre().getLitbas() == StatutEmplacement.RESERVER) && (reservation.getChambre().getLitmezzanine() == StatutEmplacement.RESERVER) ) {
                reservation.getChambre().setStatut(StatutChambre.RESERVER);
            }
            reservation.setDateReservation(LocalDateTime.now());
            chambreRepository.save(reservation.getChambre());
            return reservationRepository.save(reservation);
        }

        @Override
        public boolean verifierReservationEtudiant(Etudiant etudiant) {
            return reservationRepository.existsByEtudiantId(etudiant.getId());
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

}

