package com.example.repository;

import com.example.model.Salle;

import javax.persistence.EntityManager;
import java.util.List;

public class SalleRepositoryImpl implements SalleRepository {
    private final EntityManager em;

    public SalleRepositoryImpl(EntityManager em) {
        this.em = em;
    }

    @Override
    public void save(Salle salle) {
        if (salle.getId() == null) {
            em.persist(salle);
        } else {
            em.merge(salle);
        }
    }

    @Override
    public Salle findById(Long id) {
        return em.find(Salle.class, id);
    }

    @Override
    public List<Salle> findAll() {
        return em.createQuery("SELECT s FROM Salle s", Salle.class).getResultList();
    }

    @Override
    public void delete(Salle salle) {
        Salle managed = em.contains(salle) ? salle : em.merge(salle);
        em.remove(managed);
    }
}
