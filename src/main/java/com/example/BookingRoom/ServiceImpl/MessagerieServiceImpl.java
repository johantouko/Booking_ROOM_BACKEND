package com.example.BookingRoom.ServiceImpl;

import com.example.BookingRoom.Entities.Etudiant;
import com.example.BookingRoom.Entities.Reservation;
import com.example.BookingRoom.Services.MessagerieService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessagerieServiceImpl implements MessagerieService {

    private final JavaMailSender mailSender;

    @Override
    public void envoyerEmailEnAttente(Etudiant etudiant) {
        String sujet = "📬 Votre demande de réservation a été enregistrée";
        String corps = """
                Bonjour %s,

                Nous avons bien reçu votre demande de réservation de chambre à l’Institut Universitaire Saint Jean.

                👉 Votre demande est *en attente de validation*.

                ⚠ Vous disposez d’un *délai de 48 heures* pour :
                1. Effectuer le paiement du loyer et de la caution
                2. Et *déposer le reçu de paiement* à la comptabilité

                Sans dépôt de reçu dans ce délai, votre demande sera automatiquement annulée.

                Merci pour votre réactivité,
                L’équipe Logement
                Institut Universitaire Saint Jean
                """.formatted(etudiant.getNom());
        envoyer(etudiant.getEmail(), sujet, corps);
    }

    @Override
    public void envoyerEmailEcheance(Etudiant etudiant, String dateLimite) {
        String sujet = "⏰ Dernier rappel : déposez votre reçu dans les 12h";
        String corps = """
                Bonjour %s,

                Votre demande de réservation est toujours *en attente de validation*, et le délai de 48 heures arrive à expiration.

                📌 Pour finaliser votre demande :
                - Vous devez avoir *effectué le paiement*
                - Et *déposé le reçu de paiement à la comptabilité* avant %s

                Passé ce délai, votre réservation sera automatiquement annulée.

                Si vous avez déjà déposé le reçu, merci d’ignorer ce message.

                Cordialement,
                Service Logement
                Institut Universitaire Saint Jean
                """.formatted(etudiant.getNom(), dateLimite);
        envoyer(etudiant.getEmail(), sujet, corps);
    }

    @Override
    public void envoyerEmailValidation(Reservation reservation) {
        String sujet = "🎉 Votre réservation est validée !";
        String corps = """
                Bonjour %s,

                Votre paiement a été vérifié et votre reçu validé ✅
                Nous vous confirmons que votre réservation de chambre est *définitivement acceptée*.

                📍 Informations :
                - École : %s
                - Filière : %s
                - Chambre : %s
                - Emplacement : %s

                Vous recevrez votre clé par le service logement.

                Merci de votre confiance,
                Service Logement
                Institut Universitaire Saint Jean du Cameroun
                """.formatted(reservation.getEtudiant().getNom(), reservation.getEtudiant().getFiliere().getEcole().getNom(),
                reservation.getEtudiant().getFiliere().getNom(), reservation.getChambre().getNumero(), reservation.getEmplacementchambre());
        envoyer(reservation.getEtudiant().getEmail() , sujet, corps);
    }

    private void envoyer(String to, String sujet, String corps) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("ton.email@gmail.com"); // À remplacer par ton adresse d'envoi
        message.setTo(to);
        message.setSubject(sujet);
        message.setText(corps);
        mailSender.send(message);
    }
}
