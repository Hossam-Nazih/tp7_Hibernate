package com.example;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.model.Utilisateur;
import com.example.service.ReferentielService;
import com.example.service.ReferentielServiceImpl;
import com.example.service.ReservationService;
import com.example.service.ReservationServiceImpl;
import org.junit.Before;
import org.junit.Test;

import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class OptimisticLockingTest {

    // La base H2 est partagée entre les tests : noms et emails doivent rester uniques
    private static final AtomicInteger COMPTEUR = new AtomicInteger();

    private final ReferentielService referentiel = new ReferentielServiceImpl();
    private final ReservationService service = new ReservationServiceImpl();

    private Salle salle1;
    private Salle salle2;
    private Reservation reservation;

    @Before
    public void preparer() {
        int n = COMPTEUR.incrementAndGet();
        salle1 = referentiel.creerSalle(new Salle("Salle test " + n + "-1", 10, 0));
        salle2 = referentiel.creerSalle(new Salle("Salle test " + n + "-2", 30, 1));
        Utilisateur u = referentiel.creerUtilisateur(new Utilisateur("Testeur " + n, "test" + n + "@example.com"));
        LocalDateTime debut = LocalDateTime.now().plusDays(2);
        reservation = service.creerReservation(new Reservation(salle1, u, debut, debut.plusHours(1), "Test"));
    }

    @Test
    public void laVersionAugmenteAChaqueModification() {
        assertEquals(0, reservation.getNumeroVersion());
        Reservation copie = service.trouverParId(reservation.getId());
        copie.setObjet("Modifié");
        assertEquals(1, service.modifierReservation(copie).getNumeroVersion());
    }

    @Test(expected = OptimisticLockException.class)
    public void uneCopieObsoleteEstRefusee() {
        Reservation tx1 = service.trouverParId(reservation.getId());
        Reservation tx2 = service.trouverParId(reservation.getId());
        tx1.setSalleReservee(salle2);
        service.modifierReservation(tx1);
        tx2.setObjet("Trop tard");
        service.modifierReservation(tx2);
    }

    @Test
    public void leRetryAppliqueLaModificationSurLaDerniereVersion() {
        Reservation tx1 = service.trouverParId(reservation.getId());
        Reservation tx2 = service.trouverParId(reservation.getId());
        tx1.setObjet("Par tx1");
        service.modifierReservation(tx1);

        tx2.setSalleReservee(salle2);
        tx2.setObjet("Par tx2");
        Reservation resultat = new ReservationRetryHandler(service).modifierAvecRetry(tx2);

        assertEquals("Par tx2", resultat.getObjet());
        assertEquals(salle2.getId(), resultat.getSalleReservee().getId());
        assertEquals(2, resultat.getNumeroVersion());
    }

    @Test(expected = OptimisticLockException.class)
    public void uneSalleObsoleteEstRefusee() {
        Salle copie1 = referentiel.trouverSalle(salle1.getId());
        Salle copie2 = referentiel.trouverSalle(salle1.getId());
        copie1.setCapacite(15);
        referentiel.modifierCapacite(copie1);
        copie2.setCapacite(50);
        referentiel.modifierCapacite(copie2);
    }
}
