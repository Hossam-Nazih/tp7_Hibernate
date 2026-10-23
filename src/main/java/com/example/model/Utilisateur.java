package com.example.model;

import javax.persistence.*;

/**
 * Personne qui effectue une réservation.
 * Pas de @Version ici : un utilisateur est rarement modifié en concurrence.
 */
@Entity
@Table(name = "utilisateurs")
public class Utilisateur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_complet", nullable = false)
    private String nomComplet;

    @Column(nullable = false, unique = true)
    private String email;

    public Utilisateur() {}

    public Utilisateur(String nomComplet, String email) {
        this.nomComplet = nomComplet;
        this.email = email;
    }

    public Long getId() { return id; }
    public String getNomComplet() { return nomComplet; }
    public void setNomComplet(String nomComplet) { this.nomComplet = nomComplet; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() {
        return nomComplet + " <" + email + ">";
    }
}
