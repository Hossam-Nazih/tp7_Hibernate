package com.example.service;

import com.example.model.Salle;
import com.example.model.Utilisateur;
import com.example.util.JpaUtil;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class ReferentielServiceImpl implements ReferentielService {

    private final EntityManagerFactory emf = JpaUtil.getEntityManagerFactory();

    @Override
    public Salle creerSalle(Salle salle) {
        persister(salle);
        return salle;
    }

    @Override
    public Utilisateur creerUtilisateur(Utilisateur utilisateur) {
        persister(utilisateur);
        return utilisateur;
    }

    @Override
    public Salle trouverSalle(Long id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Salle.class, id);
        } finally {
            em.close();
        }
    }

    @Override
    public Salle modifierCapacite(Salle salle) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            // merge() d'un objet détaché : Hibernate compare lui-même numero_version
            // et lève une OptimisticLockException si la salle a changé entre-temps
            Salle managed = em.merge(salle);
            em.flush();
            em.getTransaction().commit();
            return managed;
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }

    private void persister(Object entite) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(entite);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw e;
        } finally {
            em.close();
        }
    }
}
