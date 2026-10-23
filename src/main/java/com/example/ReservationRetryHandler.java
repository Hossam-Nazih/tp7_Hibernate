package com.example;

import com.example.model.Reservation;
import com.example.service.ReservationService;

import javax.persistence.OptimisticLockException;

/**
 * Réessaie une modification en cas de conflit optimiste.
 * À chaque échec, on recharge la dernière version en base et on y réapplique
 * les changements demandés par l'utilisateur (stratégie "le dernier gagne").
 */
public class ReservationRetryHandler {

    private static final int MAX_TENTATIVES = 3;
    private static final long PAUSE_MS = 50;

    private final ReservationService service;

    public ReservationRetryHandler(ReservationService service) {
        this.service = service;
    }

    public Reservation modifierAvecRetry(Reservation souhaitee) {
        Reservation aEnvoyer = souhaitee;
        for (int tentative = 1; tentative <= MAX_TENTATIVES; tentative++) {
            try {
                return service.modifierReservation(aEnvoyer);
            } catch (OptimisticLockException e) {
                System.out.println("Conflit détecté (tentative " + tentative + "/" + MAX_TENTATIVES + ") : " + e.getMessage());
                if (tentative == MAX_TENTATIVES) {
                    break;
                }
                pause(tentative);
                // On repart de l'état actuel en base (nouvelle version)...
                aEnvoyer = service.trouverParId(souhaitee.getId());
                if (aEnvoyer == null) {
                    throw new IllegalStateException("Réservation " + souhaitee.getId() + " supprimée entre-temps");
                }
                // ...et on y recopie les valeurs voulues, sans toucher au numéro de version rechargé
                aEnvoyer.setSalleReservee(souhaitee.getSalleReservee());
                aEnvoyer.setHeureDebut(souhaitee.getHeureDebut());
                aEnvoyer.setHeureFin(souhaitee.getHeureFin());
                aEnvoyer.setObjet(souhaitee.getObjet());
            }
        }
        throw new OptimisticLockException("Échec après " + MAX_TENTATIVES + " tentatives");
    }

    // Attente croissante pour laisser l'autre transaction terminer
    private void pause(int tentative) {
        try {
            Thread.sleep(PAUSE_MS * tentative);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Retry interrompu", e);
        }
    }
}
