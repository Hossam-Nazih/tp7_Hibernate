package com.example.service;

import com.example.model.Salle;
import com.example.model.Utilisateur;

/**
 * Gestion des données de référence : salles et utilisateurs.
 */
public interface ReferentielService {
    Salle creerSalle(Salle salle);
    Utilisateur creerUtilisateur(Utilisateur utilisateur);
    Salle trouverSalle(Long id);
    // Modifie la capacité en vérifiant la version connue par l'appelant
    Salle modifierCapacite(Salle salle);
}
