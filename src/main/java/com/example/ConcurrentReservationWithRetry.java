package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;
import com.example.service.ReferentielService;
import com.example.service.ReferentielServiceImpl;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;
import com.example.util.JpaUtil;

import java.time.LocalDateTime;

/**
 * Même conflit que ConcurrentReservationSimulator, résolu cette fois par ReservationRetryHandler.
 */
public class ConcurrentReservationWithRetry {

    public static void main(String[] args) {
        ReferentielService referentiel = new ReferentielServiceImpl();
        ReservationService service = new ReservationServiceImpl();
        ReservationRetryHandler retryHandler = new ReservationRetryHandler(service);

        Salle salleA = referentiel.creerSalle(new Salle("Salle A", 20, 1));
        Salle salleB = referentiel.creerSalle(new Salle("Salle B", 40, 2));
        Salle salleC = referentiel.creerSalle(new Salle("Salle C", 12, 0));
        Utilisateur hossam = referentiel.creerUtilisateur(new Utilisateur("Hossam Nazih", "hossam@example.com"));

        LocalDateTime debut = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0).withSecond(0).withNano(0);
        Reservation reservation = service.creerReservation(
                new Reservation(salleA, hossam, debut, debut.plusHours(2), "Cours microservices"));

        Reservation tx1 = service.trouverParId(reservation.getId());
        Reservation tx2 = service.trouverParId(reservation.getId());

        tx1.setSalleReservee(salleB);
        service.modifierReservation(tx1);
        System.out.println("Transaction 1 : modification réussie.");

        // tx2 est obsolète : le premier essai échoue, le second repart de la version à jour
        tx2.setSalleReservee(salleC);
        try {
            Reservation resultat = retryHandler.modifierAvecRetry(tx2);
            System.out.println("Transaction 2 : modification réussie après retry -> " + resultat);
        } catch (Exception e) {
            System.err.println("Transaction 2 : échec après retries - " + e.getMessage());
        }

        JpaUtil.fermer();
    }
}
