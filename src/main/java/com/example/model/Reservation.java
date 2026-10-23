package com.example.model;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * Réservation d'une salle par un utilisateur sur un créneau.
 * Le champ numeroVersion active le verrouillage optimiste.
 */
@Entity
@Table(name = "reservations")
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Plusieurs réservations peuvent viser la même salle
    @ManyToOne(optional = false)
    @JoinColumn(name = "salle_id")
    private Salle salleReservee;

    @ManyToOne(optional = false)
    @JoinColumn(name = "utilisateur_id")
    private Utilisateur reservePar;

    @Column(name = "heure_debut", nullable = false)
    private LocalDateTime heureDebut;

    @Column(name = "heure_fin", nullable = false)
    private LocalDateTime heureFin;

    // Motif libre de la réservation (réunion, cours, soutenance...)
    private String objet;

    // Hibernate ajoute "and numero_version=?" à chaque UPDATE puis l'incrémente ;
    // si aucune ligne n'est mise à jour, une OptimisticLockException est levée
    @Version
    @Column(name = "numero_version")
    private int numeroVersion;

    public Reservation() {}

    public Reservation(Salle salleReservee, Utilisateur reservePar,
                       LocalDateTime heureDebut, LocalDateTime heureFin, String objet) {
        this.salleReservee = salleReservee;
        this.reservePar = reservePar;
        this.heureDebut = heureDebut;
        this.heureFin = heureFin;
        this.objet = objet;
    }

    public Long getId() { return id; }
    public Salle getSalleReservee() { return salleReservee; }
    public void setSalleReservee(Salle salleReservee) { this.salleReservee = salleReservee; }
    public Utilisateur getReservePar() { return reservePar; }
    public void setReservePar(Utilisateur reservePar) { this.reservePar = reservePar; }
    public LocalDateTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalDateTime heureDebut) { this.heureDebut = heureDebut; }
    public LocalDateTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalDateTime heureFin) { this.heureFin = heureFin; }
    public String getObjet() { return objet; }
    public void setObjet(String objet) { this.objet = objet; }
    // Pas de setter : seul Hibernate fait évoluer la version
    public int getNumeroVersion() { return numeroVersion; }

    @Override
    public String toString() {
        return "Reservation#" + id + " [" + salleReservee.getNomSalle() + ", " + objet
                + ", v" + numeroVersion + "]";
    }
}
