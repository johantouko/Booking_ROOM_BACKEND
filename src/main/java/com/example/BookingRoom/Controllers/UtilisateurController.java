package com.example.BookingRoom.Controllers;


import com.example.BookingRoom.Entities.DTO.AuthentificationDTO;
import com.example.BookingRoom.Entities.Utilisateur;
import com.example.BookingRoom.Services.UtilisateurService;
import com.example.BookingRoom.jwt.JwtService;
import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
//@AllArgsConstructor
@RestController
//@RequestMapping("/utilisateur")
public class UtilisateurController {
    private AuthenticationManager authenticationManager;
    private UtilisateurService utilisateurService;
    private JwtService jwtService;
    //private PasswordEncoder passwordEncoder;

    public UtilisateurController(AuthenticationManager authenticationManager, UtilisateurService utilisateurService, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.utilisateurService = utilisateurService;
        this.jwtService = jwtService;
    }

    @PostMapping(path = "/connexion")
    public Map<String, String> connexion(@RequestBody AuthentificationDTO authentificationDTO){
        System.out.println("cool..........");
        final Authentication authenticate = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authentificationDTO.username(), authentificationDTO.password())
        );
        if(authenticate.isAuthenticated()){
            Map<String, String> response = new HashMap<>();
            Map<String, String> maptoken =  this.jwtService.generate(authentificationDTO.username());
            response.put("message", "Utilisateur connecté avec succès");
            response.put("username", authentificationDTO.username());
            response.put("token", maptoken.get("token"));
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(authentificationDTO.username());
            response.put("roles", utilisateur.concatenateRoles(utilisateur.getRoles()));
            utilisateur.setToken(maptoken.get("token"));
            utilisateurService.modifier(utilisateur);
            return response;
        } else {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", "Échec de l'authentification");
            return errorResponse;
        }
    }

    @GetMapping(path = "/verifiertoken")
    public Map<String, Object> isTokenCorrect(@RequestParam String token) {
        Map<String, Object> response = new HashMap<>();
        if(token.isEmpty()) {
            response.put("message", "Le token envoyé est vide");
            response.put("valide", false);
            return response;
        }
        try {
            String username = jwtService.extractUsername(token);
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(username);
            System.out.println(utilisateur.getEmail());
            if(!Objects.equals(utilisateur.getToken(), token)) {
                response.put("message", "Le token est invalide");
                response.put("valide", false);
                return response;
            }
        } catch (Exception e) {
            response.put("message", "Le token est invalide");
            response.put("valide", false);
            return response;
        }
        if(jwtService.isTokenExpired(token)){
            response.put("message", "Le token est expiré");
            response.put("valide", false);
            return response;
        }
        response.put("message", "Le token est valide");
        response.put("valide", true);
        return response;
    }

    @GetMapping(path = "/getconnecteduser")
    public Utilisateur getConnectedUtilisateur(){
        System.out.println("getconnecteduser....");
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof UserDetails){
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(username);
            return utilisateur;
        }else {
            System.out.println("Aucun user n'est connecté");
            return null;
        }
    }

    @PutMapping(path = "/deconnexion")
    public void deconnexion(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if(authentication != null && authentication.getPrincipal() instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String username = userDetails.getUsername();
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(username);
            utilisateur.setToken(null);
            utilisateurService.modifier(utilisateur);
        }
    }


    @GetMapping("/getuser")
    public Utilisateur getUtilisateurById(int id) {
        return utilisateurService.getUtilisateurById(id);
    }

    @PostMapping(path = "/creeruser")
    public Map<String, Object> createUtilisateur(@RequestBody Utilisateur utilisateur){
        Map<String, Object> response = new HashMap<>();
        Utilisateur cu = getConnectedUtilisateur();

        if (utilisateurService.emailExists(utilisateur.getEmail())) {
            response.put("message", "Cet email est déjà utilisé.");
            response.put("success", false);
            return response;
        }

        Utilisateur nouvelUtilisateur = utilisateurService.creer(utilisateur);
        boolean utilisateurCree = (nouvelUtilisateur != null);
        if (utilisateurCree) {
            utilisateurService.ajouterRole(nouvelUtilisateur.getEmail(), "Administrateur");
            response.put("success", true);
            response.put("message", "Utilisateur créé avec succès.");
        }
        else{
            response.put("success", false);
            response.put("message", "Enregistrement échoué. Une erreur est survenue");
        }
        return response;
    }

    @PutMapping(path = "/updateuser")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, Object> updateUtilisateur(@RequestBody Utilisateur utilisateur){
        Map<String, Object> response = new HashMap<>();
        Map<String, Object> errors = new HashMap<>();
        Utilisateur cu = getConnectedUtilisateur();

        Utilisateur existingUtilisateur = utilisateurService.getUtilisateurById(utilisateur.getId());
        if (existingUtilisateur == null) {
            response.put("message", "Utilisateur non trouvé.");
            response.put("success", false);
            return response;
        }
        if ((!Objects.equals(existingUtilisateur.getEmail(), utilisateur.getEmail())) && (utilisateurService.emailExists(utilisateur.getEmail()))) {
            response.put("message", "Cet email est déjà utilisé.");
            response.put("success", false);
            return response;
        }

        existingUtilisateur.setNom(utilisateur.getNom());
        Utilisateur updatedUtilisateur = utilisateurService.modifier(existingUtilisateur);
        boolean utilisateurModifie = (updatedUtilisateur != null);
        if (utilisateurModifie) {
            response.put("message", "Utilisateur mis à jour avec succès.");
            response.put("success", true);
            //emailSenderService.sendMailCreateEtudiant(nouvelUtilisateur, "Création de compte Etudiant");
        }
        else{
            response.put("success", false);
            response.put("message", "Erreur survenue lors de la modification");
        }
        return response;
    }

    @PostMapping(path = "/ajouterRole")
    public void ajouterRole(@RequestBody RoleUtilisateurForm roleUtilisateurForm){
        utilisateurService.ajouterRole(roleUtilisateurForm.getEmail(), roleUtilisateurForm.getNom_role());
    }
}

@Data
@Getter
class RoleUtilisateurForm{
    private String email;
    private String nom_role;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNom_role() {
        return nom_role;
    }

    public void setNom_role(String nom_role) {
        this.nom_role = nom_role;
    }
}

