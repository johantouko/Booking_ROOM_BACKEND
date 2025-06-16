package com.example.BookingRoom.confs;


import com.example.BookingRoom.Entities.Utilisateur;
import com.example.BookingRoom.Repository.UtilisateurRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByEmail(username);
        if (utilisateur == null){
            throw new UsernameNotFoundException("Cet utilisateur n'existe pas");
        }
        return new CustomUserDetails(utilisateur);
    }
}
