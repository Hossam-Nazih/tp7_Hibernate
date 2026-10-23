package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;
import com.example.service.ReferentielService;
import com.example.service.ReferentielServiceImpl;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;
import com.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;

/**
 * Provoque volontairement des conflits optimistes, sans stratégie de reprise.
 */
public class ConcurrentReservationSimulator {

    public static void main(String[] args) {
        ReferentielService referentiel = new ReferentielServiceImpl();
        ReservationService service = new ReservationServiceImpl();

        Salle salleA = referentiel.creerSalle(new Salle("Salle A", 20, 1));
        Salle salleB = referentiel.creerSalle(new Salle("Salle B", 40, 2));
        Salle salleC = referentiel.creerSalle(new Salle("Salle C", 12, 0));
        Utilisateur hossam = referentiel.creerUtilisateur(new Utilisateur("Hossam Nazih", "hossam@example.com"));

        LocalDateTime debut = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        Reservation reservation = service.creerReservation(
                new Reservation(salleA, hossam, debut, debut.plusHours(2), "Réunion d'équipe"));
        System.out.println("Créée : " + reservation);

        // --- Scénario 1 : deux lectures successives, puis deux modifications ---
        // Chaque utilisateur garde une copie détachée portant la version lue (v0)
        Reservation tx1 = service.trouverParId(reservation.getId());
        Reservation tx2 = service.trouverParId(reservation.getId());

        tx1.setSalleReservee(salleB);
        service.modifierReservation(tx1);
        System.out.println("Transaction 1 : modification réussie -> " + service.trouverParId(reservation.getId()));

        // tx2 porte toujours v0 alors que la base est en v1 : refus
        try {
            tx2.setSalleReservee(salleC);
            service.modifierReservation(tx2);
            System.out.println("Transaction 2 : modification réussie.");
        } catch (OptimisticLockException e) {
            System.err.println("Transaction 2 : conflit détecté - " + e.getMessage());
        }

        // --- Scénario 2 : deux EntityManagers ouverts en même temps ---
        // Ici c'est Hibernate qui détecte le conflit via "update ... where id=? and numero_version=?"
        EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();
        EntityManager em1 = emf.createEntityManager();
        EntityManager em2 = emf.createEntityManager();
        try {
            em1.getTransaction().begin();
            em2.getTransaction().begin();
            Reservation r1 = em1.find(Reservation.class, reservation.getId());
            Reservation r2 = em2.find(Reservation.class, reservation.getId());

            r1.setObjet("Soutenance de stage");
            em1.getTransaction().commit();
            System.out.println("EntityManager 1 : commit réussi (version " + r1.getNumeroVersion() + ")");

            r2.setObjet("Atelier JPA");
            em2.flush();
            em2.getTransaction().commit();
            System.out.println("EntityManager 2 : commit réussi.");
        } catch (OptimisticLockException e) {
            System.err.println("EntityManager 2 : conflit détecté par Hibernate - " + e.getMessage());
            em2.getTransaction().rollback();
        } finally {
            em1.close();
            em2.close();
        }

        // --- Scénario 3 : conflit sur une autre entité versionnée (Salle) ---
        Salle copie1 = referentiel.trouverSalle(salleA.getId());
        Salle copie2 = referentiel.trouverSalle(salleA.getId());
        copie1.setCapacite(25);
        referentiel.modifierCapacite(copie1);
        System.out.println("Gestionnaire 1 : capacité de " + copie1.getNomSalle() + " passée à 25");
        try {
            copie2.setCapacite(30);
            referentiel.modifierCapacite(copie2);
            System.out.println("Gestionnaire 2 : modification réussie.");
        } catch (OptimisticLockException e) {
            System.err.println("Gestionnaire 2 : conflit détecté - " + e.getMessage());
        }

        System.out.println("État final :");
        service.listerReservations().forEach(r -> System.out.println("  " + r));
        System.out.println("  " + referentiel.trouverSalle(salleA.getId()));

        JpaUtil.fermer();
    }
}
