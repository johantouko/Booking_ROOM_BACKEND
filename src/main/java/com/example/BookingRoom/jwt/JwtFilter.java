package com.example.BookingRoom.jwt;


import com.example.BookingRoom.Entities.Utilisateur;
import com.example.BookingRoom.Services.UtilisateurService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

@Service
public class JwtFilter extends OncePerRequestFilter {
    private UtilisateurService utilisateurService;
    private JwtService jwtService;

    public JwtFilter(UtilisateurService utilisateurService, JwtService jwtService) {
        this.utilisateurService = utilisateurService;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = null;
        String username = null;
        boolean isTokenExpired = true;
        final String authorization = request.getHeader("Authorization");
        System.out.println("authorization : "+authorization);
        if(authorization != null && authorization.startsWith("Bearer ")){
            token = authorization.substring(7);
            isTokenExpired = jwtService.isTokenExpired(token);
            username = jwtService.extractUsername(token);
        }

        if (!isTokenExpired && username != null && SecurityContextHolder.getContext().getAuthentication() == null){
            Utilisateur utilisateur = utilisateurService.getUtilisateurByEmail(username);
            Collection<GrantedAuthority> authorities =  new ArrayList<>();
            utilisateur.getRoles().forEach(r->{
                authorities.add(new SimpleGrantedAuthority(r.getNom()));
            });
            UserDetails userDetails = new User(utilisateur.getEmail(), utilisateur.getMotDePasse(), authorities);
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        filterChain.doFilter(request, response);
    }

    public Boolean isTokenExpired(String token) { //retoune true si le token a expirer
        try {
            System.out.println(token);
            return jwtService.isTokenExpired(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Error processing token: " + e.getMessage());
//            return false;
        }
    }
}
