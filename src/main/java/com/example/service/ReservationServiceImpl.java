package com.example.service;

import com.example.model.Reservation;
import com.example.model.Salle;
import com.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import java.util.List;

public class ReservationServiceImpl implements ReservationService {

    private final EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

    @Override
    public Reservation creerReservation(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(reservation);
            em.getTransaction().commit();
            return reservation;
        } catch (RuntimeException e) {
            annulerSiActive(em);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation modifierReservation(Reservation reservation) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Reservation managed = em.find(Reservation.class, reservation.getId());
            if (managed == null) {
                throw new IllegalArgumentException("Réservation " + reservation.getId() + " non trouvée");
            }
            // L'objet reçu est détaché : on compare sa version avec celle en base.
            // Si elle diffère, quelqu'un a modifié la réservation depuis sa lecture.
            if (managed.getNumeroVersion() != reservation.getNumeroVersion()) {
                throw new OptimisticLockException("Version obsolète : v" + reservation.getNumeroVersion()
                        + " lue, v" + managed.getNumeroVersion() + " en base");
            }
            // find() plutôt que getReference() : un proxy non chargé serait inutilisable après em.close()
            managed.setSalleReservee(em.find(Salle.class, reservation.getSalleReservee().getId()));
            managed.setHeureDebut(reservation.getHeureDebut());
            managed.setHeureFin(reservation.getHeureFin());
            managed.setObjet(reservation.getObjet());
            // flush() exécute l'UPDATE ... where numero_version=? avant le commit :
            // si une autre transaction a écrit entre le find() et ici, l'OptimisticLockException
            // sort directement au lieu d'être enveloppée dans une RollbackException
            em.flush();
            em.getTransaction().commit();
            return managed;
        } catch (RuntimeException e) {
            annulerSiActive(em);
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Reservation trouverParId(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Reservation.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public List<Reservation> listerReservations() {
        EntityManager em = emf.createEntityManager();
        try {
            // JOIN FETCH charge salle et utilisateur en une requête, avant la fermeture de l'EntityManager
            return em.createQuery(
                    "SELECT r FROM Reservation r JOIN FETCH r.salleReservee JOIN FETCH r.reservePar ORDER BY r.id",
                    Reservation.class).getResultList();
        } finally {
            em.close();
        }
    }

    private void annulerSiActive(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}
