package com.example.service;

import com.example.model.Salle;
import java.util.List;

import java.time.LocalDateTime;
import java.util.Map;
import com.example.util.PaginationResult;

public interface SalleService {
    void saveSalle(Salle salle);
    Salle findSalleById(Long id);
    List<Salle> getAllSalles();
    void deleteSalle(Salle salle);

    List<Salle> findAvailableRooms(LocalDateTime start, LocalDateTime end);
    List<Salle> searchRooms(Map<String, Object> criteres);
    int getTotalPages(int pageSize);
    PaginationResult<Salle> getPaginatedRooms(int page, int pageSize);
    long countRooms();
}
