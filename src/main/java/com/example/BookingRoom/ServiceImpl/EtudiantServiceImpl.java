package com.example.BookingRoom.ServiceImpl;

import com.example.BookingRoom.Entities.Ecole;
import com.example.BookingRoom.Entities.Etudiant;
import com.example.BookingRoom.Entities.Filiere;
import com.example.BookingRoom.Repository.EtudiantRepository;
import com.example.BookingRoom.Services.EtudiantService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EtudiantServiceImpl implements EtudiantService {

    private final EtudiantRepository etudiantRepository;

    public EtudiantServiceImpl(EtudiantRepository etudiantRepository) {
        this.etudiantRepository = etudiantRepository;
    }

    @Override
    public Etudiant createEtudiant(Etudiant etudiant) {
        return etudiantRepository.save(etudiant);
    }

    @Override
    public Etudiant updateEtudiant(Long id, Etudiant updatedData) {
        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Étudiant non trouvé avec l'id : " + id));

        etudiant.setNom(updatedData.getNom());
        etudiant.setWhatsappEtudiant(updatedData.getWhatsappEtudiant());
        etudiant.setWhatsappParent(updatedData.getWhatsappParent());
        etudiant.setSexe(updatedData.getSexe());
        etudiant.setEmail(updatedData.getEmail());
        etudiant.setFiliere(updatedData.getFiliere());

        return etudiantRepository.save(etudiant);
    }

    @Override
    public List<Etudiant> getAllEtudiants() {
        return etudiantRepository.findAll();
    }

    @Override
    public List<Etudiant> getEtudaintByFiliere(Filiere filiere) {
        return this.etudiantRepository.findByFiliere(filiere);
    }

    @Override
    public List<Etudiant> searchEtudiantsByName(String q) {
        return this.etudiantRepository.findByNomIsContainingIgnoreCase(q);
    }

    @Override
    public Etudiant getetudiantbyId(long idetudiant) {
        return etudiantRepository.findById(idetudiant).orElse(null);
    }

    @Override
    public boolean emailExists(String email) {
        List<Etudiant> utilisateurs = getAllEtudiants();
        return utilisateurs.stream().anyMatch(e -> e.getEmail().equals(email));
    }

    @Override
    public boolean telephoneExists(int telephone) {
        List<Etudiant> utilisateurs = getAllEtudiants();
        return utilisateurs.stream()
                .anyMatch(e -> e.getWhatsappEtudiant() == telephone);
    }

    @Override
    public boolean nameExists(String name) {
        List<Etudiant> etudiants = etudiantRepository.findAll();
        return etudiants.stream().anyMatch(e -> e.getNom().equals(name));
    }

}
