package com.example.service;

import com.example.model.Reservation;
import com.example.repository.ReservationRepository;
import java.util.List;

public class ReservationServiceImpl implements ReservationService {
    private final ReservationRepository repository;

    public ReservationServiceImpl(ReservationRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Reservation> getAllReservations() {
        return repository.findAll();
    }

    @Override
    public Reservation getReservationById(Long id) {
        return repository.findById(id);
    }

    @Override
    public void createReservation(Reservation reservation) {
        repository.save(reservation);
    }

    @Override
    public void deleteReservation(Long id) {
        repository.delete(id);
    }
}
