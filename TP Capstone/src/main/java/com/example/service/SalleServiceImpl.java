package com.example.service;

import com.example.model.Salle;
import com.example.repository.SalleRepository;

import javax.persistence.EntityManager;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Collections;
import com.example.util.PaginationResult;

public class SalleServiceImpl implements SalleService {
    private final EntityManager em;
    private final SalleRepository salleRepository;

    public SalleServiceImpl(EntityManager em, SalleRepository salleRepository) {
        this.em = em;
        this.salleRepository = salleRepository;
    }

    @Override
    public void saveSalle(Salle salle) {
        em.getTransaction().begin();
        salleRepository.save(salle);
        em.getTransaction().commit();
    }

    @Override
    public Salle findSalleById(Long id) {
        return salleRepository.findById(id);
    }

    @Override
    public List<Salle> getAllSalles() {
        return salleRepository.findAll();
    }

    @Override
    public void deleteSalle(Salle salle) {
        em.getTransaction().begin();
        salleRepository.delete(salle);
        em.getTransaction().commit();
    }

    @Override
    public List<Salle> findAvailableRooms(LocalDateTime start, LocalDateTime end) {
        // Dummy implementation, replace with real logic
        return salleRepository.findAll();
    }

    @Override
    public List<Salle> searchRooms(Map<String, Object> criteres) {
        // Dummy implementation, replace with real logic
        return salleRepository.findAll();
    }

    @Override
    public int getTotalPages(int pageSize) {
        long count = countRooms();
        return (int) Math.ceil((double) count / pageSize);
    }

    @Override
    public PaginationResult<Salle> getPaginatedRooms(int page, int pageSize) {
        // Dummy implementation, replace with real logic
        return new PaginationResult<>(salleRepository.findAll(), page, pageSize, getTotalPages(pageSize), countRooms());
    }

    @Override
    public long countRooms() {
        // Dummy implementation, replace with real logic
        return salleRepository.findAll().size();
    }
}
