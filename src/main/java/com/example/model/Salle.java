package com.example.model;

import javax.persistence.*;

/**
 * Salle pouvant être réservée. Elle est elle aussi versionnée :
 * deux gestionnaires qui modifient la capacité en même temps provoquent un conflit.
 */
@Entity
@Table(name = "salles")
public class Salle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom_salle", nullable = false, unique = true)
    private String nomSalle;

    @Column(nullable = false)
    private int capacite;

    private int etage;

    // Incrémenté par Hibernate à chaque UPDATE de la salle
    @Version
    @Column(name = "numero_version")
    private int numeroVersion;

    public Salle() {}

    public Salle(String nomSalle, int capacite, int etage) {
        this.nomSalle = nomSalle;
        this.capacite = capacite;
        this.etage = etage;
    }

    public Long getId() { return id; }
    public String getNomSalle() { return nomSalle; }
    public void setNomSalle(String nomSalle) { this.nomSalle = nomSalle; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    public int getEtage() { return etage; }
    public void setEtage(int etage) { this.etage = etage; }
    public int getNumeroVersion() { return numeroVersion; }

    @Override
    public String toString() {
        return nomSalle + " (" + capacite + " places, étage " + etage + ")";
    }
}
