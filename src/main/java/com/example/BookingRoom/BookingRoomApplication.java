package com.example.BookingRoom;

import com.example.BookingRoom.Entities.Role;
import com.example.BookingRoom.Entities.Utilisateur;
import com.example.BookingRoom.Services.RoleService;
import com.example.BookingRoom.Services.UtilisateurService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.ArrayList;

@SpringBootApplication
@EnableScheduling
public class BookingRoomApplication {

	public static void main(String[] args) {
		SpringApplication.run(BookingRoomApplication.class, args);
	}

	@Bean
	CommandLineRunner start(UtilisateurService us, RoleService rs){
		return args -> {

			// Vérifie si le rôle existe déjà
			if (rs.getByNom("Administrateur") == null) {
				rs.creer(new Role(0, "Administrateur"));
			}

			// Création de l'utilisateur Constantin
			if (us.getUtilisateurByEmail("constantin.nitcheu@gmail.com") == null) {
				us.creer(new Utilisateur(0, "constantin Nitcheu", "constantin.nitcheu@gmail.com", "Changeme@2025"));
				us.creer(new Utilisateur(0, "johan Touko", "johan.touko@gmail.com", "Changeme@2025"));
			}

			// Vérifie si les utilisateurs ont déjà le rôle avant de l’ajouter
			if (!us.aDejaLeRole("constantin.nitcheu@gmail.com", "Administrateur")) {
				us.ajouterRole("constantin.nitcheu@gmail.com", "Administrateur");
				us.ajouterRole("johan.touko@gmail.com", "Administrateur");
			}

		};
	}

}
