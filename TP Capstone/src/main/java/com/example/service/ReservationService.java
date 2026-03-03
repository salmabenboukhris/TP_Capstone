package com.example.service;

import com.example.model.Reservation;
import java.util.List;

public interface ReservationService {
    List<Reservation> getAllReservations();
    Reservation getReservationById(Long id);
    void createReservation(Reservation reservation);
    void deleteReservation(Long id);
}
