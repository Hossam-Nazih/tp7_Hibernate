package com.example.service;

import com.example.model.Reservation;
import java.util.List;

/**
 * Opérations CRUD sur les réservations.
 */
public interface ReservationService {
    Reservation creerReservation(Reservation reservation);
    // Lève une OptimisticLockException si la réservation a changé depuis sa lecture
    Reservation modifierReservation(Reservation reservation);
    Reservation trouverParId(Long id);
    List<Reservation> listerReservations();
}
