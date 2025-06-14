package com.example.BookingRoom.ServiceImpl;

import com.example.BookingRoom.Entities.Chambre;
import com.example.BookingRoom.Entities.Ecole;
import com.example.BookingRoom.Entities.Filiere;
import com.example.BookingRoom.Repository.ChambreRepository;
import com.example.BookingRoom.Repository.FiliereRepository;
import com.example.BookingRoom.Services.FiliereService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FiliereServiceImpl implements FiliereService {

    private final FiliereRepository filiereRepository;
    private final ChambreRepository chambreRepository;

    public FiliereServiceImpl(FiliereRepository filiereRepository, ChambreRepository chambreRepository) {
        this.filiereRepository = filiereRepository;
        this.chambreRepository = chambreRepository;
    }

    @Override
    public Filiere createFiliere(Filiere filiere) {
        filiere.setNombreChambresDisponibles(filiere.getNombreChambres());
        return filiereRepository.save(filiere);
    }

    @Override
    public Filiere updatefiliere(Filiere filiere) {
        return filiereRepository.save(filiere);
    }

    @Override
    public Filiere findbyId(long filiere_id) {
        return filiereRepository.findById(filiere_id).orElse(null);
    }


    @Override
    public boolean nomFiliereExists(String name) {
        List<Filiere> filieres = filiereRepository.findAll();
        return filieres.stream().anyMatch(e -> e.getNom().equals(name));
    }

    @Override
    public List<Filiere> getAllFiliere() {
        return filiereRepository.findAll();
    }

    @Override
    public List<Filiere> getfilierebyecole(Ecole ecole) {
        return filiereRepository.findByEcole(ecole);
    }

    @Override
    public int getTotalChambresByEcole(Long ecoleId) {
        List<Filiere> filieres = filiereRepository.findByEcole_Id(ecoleId);
        return filieres.stream()
                .mapToInt(Filiere::getNombreChambres)
                .sum();
    }

}
