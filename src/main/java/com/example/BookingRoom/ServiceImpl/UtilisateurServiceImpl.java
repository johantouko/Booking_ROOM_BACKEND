package com.example.BookingRoom.ServiceImpl;

import com.example.BookingRoom.Entities.Role;
import com.example.BookingRoom.Entities.Utilisateur;
import com.example.BookingRoom.Repository.RoleRepository;
import com.example.BookingRoom.Repository.UtilisateurRepository;
import com.example.BookingRoom.Services.UtilisateurService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UtilisateurServiceImpl implements UtilisateurService {
    private UtilisateurRepository utilisateurRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;

    public UtilisateurServiceImpl(UtilisateurRepository utilisateurRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.utilisateurRepository = utilisateurRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Utilisateur creer(Utilisateur utilisateur) {
        utilisateur.setMotDePasse("Changeme@2025");
        String pw = utilisateur.getMotDePasse();
        utilisateur.setMotDePasse(passwordEncoder.encode(pw));
        return utilisateurRepository.save(utilisateur);
    }

    @Override
    public void ajouterRole(String email, String nom_role) {
        Utilisateur utilisateur = getUtilisateurByEmail(email);
        System.out.println(utilisateur.getNom());
        Role role = roleRepository.findByNom(nom_role);
        utilisateur.getRoles().add(role);
        for (Role r : utilisateur.getRoles()){
            System.out.println(role.getNom());
        }
        utilisateurRepository.save(utilisateur);
    }

    @Override
    public Utilisateur getUtilisateurByEmail(String email) {
        return utilisateurRepository.findByEmail(email);
    }

    @Override
    public List<Utilisateur> lister() {
        return utilisateurRepository.findAll();
    }

    @Override
    public Utilisateur getUtilisateurById(int id) {
        return utilisateurRepository.findById(id).orElse(null);
    }

    @Override
    public boolean emailExists(String email) {
        List<Utilisateur> utilisateurs = utilisateurRepository.findAll();
        return utilisateurs.stream().anyMatch(e -> e.getEmail().equals(email));
    }


    @Override
    public Utilisateur modifier(Utilisateur utilisateur) {
        return this.utilisateurRepository.save(utilisateur);
    }


    public Utilisateur getConnectedUtilisateur(){
        System.out.println("getconnecteduser....");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof UserDetails){
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Utilisateur utilisateur = getUtilisateurByEmail(username);
            return utilisateur;
        }else {
            System.out.println("Aucun user n'est connecté");
            return null;
        }
    }

    @Override
    public boolean aDejaLeRole(String email, String roleName) {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(email);
        return utilisateur.getRoles().stream()
                .anyMatch(r -> r.getNom().equalsIgnoreCase(roleName));
    }


}
