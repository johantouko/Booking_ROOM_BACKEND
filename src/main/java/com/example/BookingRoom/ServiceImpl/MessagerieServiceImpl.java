package com.example.BookingRoom.ServiceImpl;

import com.example.BookingRoom.Entities.Etudiant;
import com.example.BookingRoom.Entities.Reservation;
import com.example.BookingRoom.Entities.ReservationEnattente;
import com.example.BookingRoom.Entities.User;
import com.example.BookingRoom.Services.MessagerieService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class MessagerieServiceImpl implements MessagerieService {

    private final JavaMailSender mailSender;

    @Override
    public void envoyerEmailEnAttente(Etudiant etudiant, LocalDateTime localDateTime) {
        String sujet = "📬 Votre demande de réservation a été enregistrée";
        String corps = """
        <html>
        <body>
        Bonjour %s,<br><br>

        Nous avons bien reçu votre demande de réservation de chambre à l’Institut Universitaire Saint Jean.<br><br>

        👉 Votre demande est <strong>en attente de validation</strong>.<br>

        ⚠ Vous disposez d’un <strong>délai de 48 heures</strong> à échéance <strong>%s</strong> pour :<br>
        1. Effectuer le paiement du loyer et de la caution<br>
        2. Et <strong>déposer le reçu de paiement</strong> à la comptabilité<br><br>

        Sans dépôt de reçu dans ce délai, votre demande sera automatiquement annulée.<br>

        Merci pour votre réactivité,<br>
        L’équipe Logement<br>
        Institut Universitaire Saint Jean
        <br><br><br>
        </body>
        </html>
        """.formatted(etudiant.getNom(), formaterDateLecture(localDateTime));

        try {
            Resource resource = new ClassPathResource("documents/Prospectus isj - résidence étudiante 2025-2026 VF.pdf");
            File fichierPdf = resource.getFile();

            envoyer(etudiant.getEmail(), sujet, corps, fichierPdf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public void envoyerEmailEcheance(Reservation reservation) {
        String sujet = "⏰ Dernier rappel : déposez votre reçu dans les 12h";
        String corps = """
                <html>
                <body>
                Bonjour %s, <br><br>

                Votre demande de réservation est toujours <strong>en attente de validation</strong>, et le délai de 48 heures arrive à expiration. <br><br>

                📌 Pour finaliser votre demande :<br>
                - Vous devez avoir <strong>effectué le paiement</strong><br>
                - Et <strong>déposé le reçu de paiement à la comptabilité</strong> avant <strong>%s </strong><br>

                Passé ce délai, votre réservation sera automatiquement annulée.<br>

                Si vous avez déjà déposé le reçu, merci d’ignorer ce message.<br><br>

                Cordialement,<br>
                Service Logement<br>
                Institut Universitaire Saint Jean
                <br><br><br>
                </body>
                </html>
                """.formatted(reservation.getEtudiant().getNom(), formaterDateLecture( reservation.getDateFinReservation()));
        envoyer(reservation.getEtudiant().getEmail(), sujet, corps, null );
    }

    @Override
    public void envoyerEmailValidation(Reservation reservation) {
        String sujet = "🎉 Votre réservation est validée !";
        String corps = """
                <html>
                <body>
                Bonjour %s, <br><br>

                Votre paiement a été vérifié et votre reçu validé ✅<br>
                Nous vous confirmons que votre réservation de chambre est <strong>définitivement acceptée</strong>. <br><br>

                📍 Informations : <br>
                - École : <strong>%s</strong><br>
                - Filière : <strong>%s</strong><br>
                - Numero hambre : <strong>%s</strong><br>
                - Emplacement : <strong>%s</strong><br>

                Vous recevrez votre clé par le service logement.<br><br>

                Merci de votre confiance,<br>
                Service Logement<br>
                Institut Universitaire Saint Jean du Cameroun<br>
                <br>
                </body>
                </html>
                """.formatted(reservation.getEtudiant().getNom(), reservation.getEtudiant().getFiliere().getEcole().getNom(),
                reservation.getEtudiant().getFiliere().getNom(), reservation.getChambre().getNumero(), reservation.getEmplacementchambre());
        envoyer(reservation.getEtudiant().getEmail() , sujet, corps, null);
    }

    @Override
    public void envoyerEmailAnnulation(Reservation reservation) {
        String sujet = "🎉 Votre réservation a été annulée !";
        String corps = """
                <html>
                <body>
                
                Bonjour %s, <br><br>
                
                Nous vous informons que votre demande de réservation de chambre a été <strong><strong>annulée automatiquement</strong>, conformément à nos règles de gestion.<br>
                
                📌 Raison de l’annulation :<br>
                Vous n’avez pas déposé votre <strong>reçu de paiement</strong> dans le délai imparti de 48 heures après soumission de la demande.<br>
                
                ⚠ Votre chambre a donc été remise en disponibilité pour d’autres étudiants.<br>
                
                
                Merci de votre compréhension, <br>
                Service Logement <br>
                Institut Universitaire Saint Jean du Cameroun<br>
                
                <br>
                </body>
                </html>
                """.formatted(reservation.getEtudiant().getNom());
        envoyer(reservation.getEtudiant().getEmail() , sujet, corps, null);
    }

    @Override
    public void envoyerEmailListeAttente(ReservationEnattente reservation) {
        String sujet = " Votre demande a été enregistrée – Vous êtes en liste d’attente !";
        String corps = """
                <html>
                <body>
                
                Bonjour %s, <br><br>
                
                Votre demande de réservation de chambre à l’Institut Universitaire Saint Jean a bien été enregistrée.<br>
                
                Cependant, toutes les chambres sont actuellement occupées. Votre dossier a donc été placé en liste d’attente.<br>
                
                👉 Vous serez automatiquement contacté(e) par email ou WhatsApp si une chambre se libère dans les prochains jours.<br>
               
                ⚠ Merci de ne pas effectuer de paiement à cette étape. Aucun reçu ne sera traité tant que votre demande n’aura pas été validée.<br>
                
                Nous vous remercions pour votre compréhension.<br>
                
                Bien cordialement,<br>
                Le service de gestion des résidences<br>
                Institut Universitaire Saint Jean<br>
            
                
                <br>
                </body>
                </html>
                """.formatted(reservation.getEtudiant().getNom());
        envoyer(reservation.getEtudiant().getEmail() , sujet, corps, null);
    }

    @Override
    public void envoyermailechenceannulation(User user, Reservation reservation) {
        String sujet = " Réservation à annuler si le paiement n’est pas confirmé. !";
        String corps = """
                <html>
                <body>
                
                Bonjour %s, <br><br>
                
                Une réservation a été soumise il y a plus de 47 heures sans que le reçu de paiement ne soit encore déposé ou validé.<br>
                Elle est donc susceptible d’être annulée automatiquement à l’expiration du délai de 48 heures.<br>
                
                Détails de la réservation concernée : <br>
                
                Nom complet : %s, <br>
                Email : %s,<br>
                Filière : %s,<br>
                Numéro de chambre : %s,<br>
                Date de soumission : %s.<br>
                
                Vous pouvez intervenir manuellement dans le tableau de bord administrateur .<br>
                
                Cordialement,<br>
                L’équipe BookingRoom<br>
               
                <br>
                </body>
                </html>
                """.formatted(user.getNom(), reservation.getEtudiant().getNom(),  reservation.getEtudiant().getEmail(), reservation.getEtudiant().getFiliere().getNom() ,
                reservation.getChambre().getNumero(), formaterDateLecture( reservation.getDateReservation()));
        envoyer(reservation.getEtudiant().getEmail() , sujet, corps, null);
    }


    private void envoyer(String to, String sujet, String corpsHtml, File pieceJointe) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("ton.email@gmail.com");
            helper.setTo(to);
            helper.setSubject(sujet);
            helper.setText(corpsHtml, true); // true → HTML enabled


            if (pieceJointe != null) {
                helper.addAttachment(pieceJointe.getName(), pieceJointe);
            }

            mailSender.send(message);
        } catch (MessagingException e) {
            e.printStackTrace(); // ou log.error(...)
        }
    }

    public static String formaterDateLecture(LocalDateTime dateTime) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEEE 'le' d MMMM yyyy 'à' HH'H'mm 'minute'", Locale.FRENCH);
        return dateTime.format(formatter);
    }

}
