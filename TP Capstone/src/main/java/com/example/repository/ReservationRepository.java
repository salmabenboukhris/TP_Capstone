package com.example.repository;

import com.example.model.Reservation;
import java.util.List;

public interface ReservationRepository {
    List<Reservation> findAll();
    Reservation findById(Long id);
    void save(Reservation reservation);
    void delete(Long id);
}
