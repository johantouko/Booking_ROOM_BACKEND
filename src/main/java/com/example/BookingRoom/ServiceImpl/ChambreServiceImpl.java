package com.example.BookingRoom.ServiceImpl;

import com.example.BookingRoom.Entities.Chambre;
import com.example.BookingRoom.Entities.Etudiant;
import com.example.BookingRoom.Entities.StatutChambre;
import com.example.BookingRoom.Entities.StatutEmplacement;
import com.example.BookingRoom.Repository.ChambreRepository;
import com.example.BookingRoom.Services.ChambreService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ChambreServiceImpl implements ChambreService {

    private final ChambreRepository chambreRepository;

    public ChambreServiceImpl(ChambreRepository chambreRepository) {
        this.chambreRepository = chambreRepository;
    }

    @Override
    public List<Chambre> getAllChambres() {
        return chambreRepository.findAll();
    }


    @Override
    public List<Chambre> getChambresByStatut(String statut) {
        return chambreRepository.findByStatut(statut);
    }

    @Override
    public Chambre createChambre(Chambre chambre) {
        chambre.setLitbas(StatutEmplacement.DISPONIBLE);
        chambre.setLitmezzanine(StatutEmplacement.DISPONIBLE);
        chambre.setStatut(StatutChambre.LIBRE);
        return chambreRepository.save(chambre);
    }

    @Override
    public Chambre getchambrebyid(long idchambre) {
        return chambreRepository.findById(idchambre).orElse(null);
    }

    @Override
    public boolean numeroexists(int numero) {
        List<Chambre> chambres = getAllChambres();
        return chambres.stream()
                .anyMatch(e -> e.getNumero() == numero);
    }

    @Override
    public Chambre majcahmabre(Chambre chambre) {
        return chambreRepository.save(chambre);
    }


}
