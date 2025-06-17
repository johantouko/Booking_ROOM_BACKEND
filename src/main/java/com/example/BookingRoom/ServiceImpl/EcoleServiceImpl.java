package com.example.BookingRoom.ServiceImpl;

import com.example.BookingRoom.Entities.Chambre;
import com.example.BookingRoom.Entities.Ecole;
import com.example.BookingRoom.Entities.Filiere;
import com.example.BookingRoom.Entities.Reservation;
import com.example.BookingRoom.Repository.ChambreRepository;
import com.example.BookingRoom.Repository.EcoleRepository;
import com.example.BookingRoom.Repository.EtudiantRepository;
import com.example.BookingRoom.Repository.FiliereRepository;
import com.example.BookingRoom.Services.EcoleService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
@Service

public class EcoleServiceImpl implements EcoleService {

    private final EcoleRepository ecoleRepository;
    private final FiliereRepository filiereRepository;
    private final EtudiantRepository etudiantRepository;
    private final ChambreRepository chambreRepository;

    public EcoleServiceImpl(EcoleRepository ecoleRepository, FiliereRepository filiereRepository, EtudiantRepository etudiantRepository, ChambreRepository chambreRepository) {
        this.ecoleRepository = ecoleRepository;
        this.filiereRepository = filiereRepository;
        this.etudiantRepository = etudiantRepository;
        this.chambreRepository = chambreRepository;
    }

    @Override
    public Ecole createEcole(Ecole ecole) {
        ecole.setNombreChambresDisponibles(ecole.getNombreChambres() - ecole.getNombreChambresIndividuelle());
        return ecoleRepository.save(ecole);
    }

    @Override
    public Ecole updateEcole(Long id, Ecole ecoleDetails) {
        Ecole ecole = ecoleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("École non trouvée avec l'id : " + id));
        ecole.setNom(ecoleDetails.getNom());
        ecole.setSigle(ecoleDetails.getSigle());
        return ecoleRepository.save(ecole);
    }

    @Override
    public List<Ecole> getAllEcoles() {
        return this.ecoleRepository.findAllByOrderByNomAsc();
    }

    @Override
    public Ecole findbyId(Long id) {
        return ecoleRepository.findById(id).orElse(null) ;
    }





    @Override
    public boolean nameExists(String name) {
        List<Ecole> ecoles = ecoleRepository.findAll();
        return ecoles.stream().anyMatch(e -> e.getNom().equals(name));
    }
//
//    @Override
//    public List<Reservation> getReservationsByEcole(Long ecoleId) {
//        List<Filiere> filieres = filiereRepository.findByEcole_Id(ecoleId);
//
//        return filieres.stream()
//                .flatMap(filiere -> etudiantRepository.findByFiliereId(filiere.getId()).stream())
//                .flatMap(etudiant -> reservationRepository.findByEtudiantId(etudiant.getId()).stream())
//                .toList();
//    }
}
