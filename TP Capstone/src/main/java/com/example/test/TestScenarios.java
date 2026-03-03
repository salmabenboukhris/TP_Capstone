package com.example.test;

import com.example.model.*;
import com.example.repository.SalleRepository;
import com.example.service.SalleService;
import com.example.service.ReservationService;
import com.example.util.PaginationResult;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.OptimisticLockException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TestScenarios {
    private final EntityManagerFactory emf;
    private final SalleService salleService;
    private final ReservationService reservationService;

    public TestScenarios(EntityManagerFactory emf, SalleService salleService, ReservationService reservationService) {
        this.emf = emf;
        this.salleService = salleService;
        this.reservationService = reservationService;
    }

    public void runAllTests() {
        System.out.println("\n=== EXÉCUTION DES SCÉNARIOS DE TEST ===\n");
        testRechercheDisponibilite();
        testRechercheMultiCriteres();
        testPagination();
        testOptimisticLocking();
        testCachePerformance();
        System.out.println("\n=== TOUS LES TESTS TERMINÉS ===\n");
    }

    private void testRechercheDisponibilite() {
        System.out.println("\n=== TEST 1: RECHERCHE DE DISPONIBILITÉ ===");
        LocalDateTime demainMatin = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        LocalDateTime demainMidi = demainMatin.plusHours(3);
        System.out.println("Recherche de salles disponibles entre " + demainMatin + " et " + demainMidi);
        List<Salle> sallesDisponibles = salleService.findAvailableRooms(demainMatin, demainMidi);
        System.out.println("Nombre de salles disponibles: " + sallesDisponibles.size());
        for (int i = 0; i < Math.min(5, sallesDisponibles.size()); i++) {
            Salle salle = sallesDisponibles.get(i);
            System.out.println("- " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ", Bâtiment: " + salle.getBatiment() + ")");
        }
        if (sallesDisponibles.size() > 5) {
            System.out.println("... et " + (sallesDisponibles.size() - 5) + " autres salles");
        }
        EntityManager em = emf.createEntityManager();
        try {
            Reservation reservation = em.createQuery("SELECT r FROM Reservation r WHERE r.statut = :statut", Reservation.class)
                    .setParameter("statut", StatutReservation.CONFIRMEE)
                    .setMaxResults(1)
                    .getSingleResult();
            System.out.println("\nRecherche de salles disponibles pendant une réservation existante:");
            System.out.println("Créneau: " + reservation.getDateDebut() + " à " + reservation.getDateFin());
            System.out.println("Salle déjà réservée: " + reservation.getSalle().getNom());
            List<Salle> sallesDispoCreneauReserve = salleService.findAvailableRooms(
                    reservation.getDateDebut(), reservation.getDateFin());
            System.out.println("Nombre de salles disponibles: " + sallesDispoCreneauReserve.size());
            System.out.println("La salle réservée est-elle exclue des résultats? " +
                    !sallesDispoCreneauReserve.contains(reservation.getSalle()));
        } finally {
            em.close();
        }
    }

    private void testRechercheMultiCriteres() {
        System.out.println("\n=== TEST 2: RECHERCHE MULTI-CRITÈRES ===");
        Map<String, Object> criteres1 = new HashMap<>();
        criteres1.put("capaciteMin", 30);
        criteres1.put("equipement", 1L);
        System.out.println("Recherche de salles avec capacité >= 30 et équipées d'un écran interactif");
        List<Salle> resultat1 = salleService.searchRooms(criteres1);
        System.out.println("Nombre de salles trouvées: " + resultat1.size());
        for (Salle salle : resultat1) {
            System.out.println("- " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ")");
            System.out.println("  Équipements: " + salle.getEquipements().size() + " équipement(s)");
        }
        Map<String, Object> criteres2 = new HashMap<>();
        criteres2.put("batiment", "Bâtiment C");
        criteres2.put("etage", 2);
        System.out.println("\nRecherche de salles dans le Bâtiment C à l'étage 2");
        List<Salle> resultat2 = salleService.searchRooms(criteres2);
        System.out.println("Nombre de salles trouvées: " + resultat2.size());
        for (Salle salle : resultat2) {
            System.out.println("- " + salle.getNom() + " (Étage: " + salle.getEtage() + ")");
        }
        Map<String, Object> criteres3 = new HashMap<>();
        criteres3.put("capaciteMin", 20);
        criteres3.put("capaciteMax", 50);
        criteres3.put("batiment", "Bâtiment B");
        criteres3.put("equipement", 6L);
        System.out.println("\nRecherche complexe: capacité entre 20 et 50, Bâtiment B, avec ordinateur fixe");
        List<Salle> resultat3 = salleService.searchRooms(criteres3);
        System.out.println("Nombre de salles trouvées: " + resultat3.size());
        for (Salle salle : resultat3) {
            System.out.println("- " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ", Bâtiment: " + salle.getBatiment() + ")");
        }
    }

    private void testPagination() {
        System.out.println("\n=== TEST 3: PAGINATION ===");
        int pageSize = 5;
        System.out.println("Pagination des salles (5 par page):");
        int totalPages = salleService.getTotalPages(pageSize);
        System.out.println("Nombre total de pages: " + totalPages);
        for (int page = 1; page <= totalPages; page++) {
            System.out.println("\nPage " + page + ":");
            PaginationResult<Salle> pageResult = salleService.getPaginatedRooms(page, pageSize);
            List<Salle> sallesPage = pageResult.getItems();
            for (Salle salle : sallesPage) {
                System.out.println("- " + salle.getNom() + " (Capacité: " + salle.getCapacite() + ", Bâtiment: " + salle.getBatiment() + ")");
            }
        }
        System.out.println("\nTest avec PaginationResult:");
        PaginationResult<Salle> paginationResult = salleService.getPaginatedRooms(1, pageSize);
        System.out.println("Page courante: " + paginationResult.getCurrentPage());
        System.out.println("Taille de la page: " + paginationResult.getPageSize());
        System.out.println("Nombre total de pages: " + paginationResult.getTotalPages());
        System.out.println("Nombre total d'éléments: " + paginationResult.getTotalItems());
        System.out.println("Page suivante disponible: " + paginationResult.hasNext());
        System.out.println("Page précédente disponible: " + paginationResult.hasPrevious());
    }

    private void testOptimisticLocking() {
        System.out.println("\n=== TEST 4: OPTIMISTIC LOCKING ===");
        EntityManager em = emf.createEntityManager();
        Reservation reservation = null;
        try {
            reservation = em.createQuery("SELECT r FROM Reservation r WHERE r.statut = :statut", Reservation.class)
                    .setParameter("statut", StatutReservation.CONFIRMEE)
                    .setMaxResults(1)
                    .getSingleResult();
            System.out.println("Réservation sélectionnée: ID=" + reservation.getId() +
                    ", Salle=" + reservation.getSalle().getNom() +
                    ", Date=" + reservation.getDateDebut());
        } finally {
            em.close();
        }
        if (reservation == null) {
            System.out.println("Aucune réservation trouvée pour le test d'optimistic locking");
            return;
        }
        final Long reservationId = reservation.getId();
        CountDownLatch latch = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(() -> {
            try {
                latch.await();
                EntityManager em1 = emf.createEntityManager();
                try {
                    em1.getTransaction().begin();
                    Reservation r1 = em1.find(Reservation.class, reservationId);
                    System.out.println("Thread 1: Réservation récupérée, version = " + r1.getVersion());
                    Thread.sleep(1000);
                    r1.setMotif("Motif modifié par Thread 1");
                    em1.merge(r1);
                    em1.getTransaction().commit();
                    System.out.println("Thread 1: Réservation mise à jour avec succès !");
                } catch (OptimisticLockException e) {
                    System.out.println("Thread 1: Conflit de verrouillage optimiste détecté !");
                    if (em1.getTransaction().isActive()) em1.getTransaction().rollback();
                } finally {
                    em1.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        executor.submit(() -> {
            try {
                latch.await();
                Thread.sleep(100);
                EntityManager em2 = emf.createEntityManager();
                try {
                    em2.getTransaction().begin();
                    Reservation r2 = em2.find(Reservation.class, reservationId);
                    System.out.println("Thread 2: Réservation récupérée, version = " + r2.getVersion());
                    r2.setDateFin(r2.getDateFin().plusHours(1));
                    em2.merge(r2);
                    em2.getTransaction().commit();
                    System.out.println("Thread 2: Réservation mise à jour avec succès !");
                } catch (OptimisticLockException e) {
                    System.out.println("Thread 2: Conflit de verrouillage optimiste détecté !");
                    if (em2.getTransaction().isActive()) em2.getTransaction().rollback();
                } finally {
                    em2.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        latch.countDown();
        executor.shutdown();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        em = emf.createEntityManager();
        try {
            Reservation finalReservation = em.find(Reservation.class, reservationId);
            System.out.println("\nÉtat final de la réservation:");
            System.out.println("ID: " + finalReservation.getId());
            System.out.println("Motif: " + finalReservation.getMotif());
            System.out.println("Date fin: " + finalReservation.getDateFin());
            System.out.println("Version: " + finalReservation.getVersion());
        } finally {
            em.close();
        }
    }

    private void testCachePerformance() {
        System.out.println("\n=== TEST 5: PERFORMANCE DU CACHE ===");
        System.out.println("\nTest d'accès répété sans cache:");
        emf.getCache().evictAll();
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            EntityManager em = emf.createEntityManager();
            try {
                Salle salle = em.find(Salle.class, (i % 15) + 1L);
                salle.getEquipements().size();
            } finally {
                em.close();
            }
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Temps d'exécution sans cache: " + (endTime - startTime) + "ms");
        System.out.println("\nTest d'accès répété avec cache:");
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            EntityManager em = emf.createEntityManager();
            try {
                Salle salle = em.find(Salle.class, (i % 15) + 1L);
                salle.getEquipements().size();
            } finally {
                em.close();
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("Temps d'exécution avec cache: " + (endTime - startTime) + "ms");
        System.out.println("\nTest de performance des requêtes avec cache:");
        emf.getCache().evictAll();
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            EntityManager em = emf.createEntityManager();
            try {
                List<Salle> salles = em.createQuery(
                        "SELECT s FROM Salle s WHERE s.capacite >= :capacite", Salle.class)
                        .setParameter("capacite", 30)
                        .getResultList();
            } finally {
                em.close();
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("Temps d'exécution des requêtes sans cache: " + (endTime - startTime) + "ms");
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            EntityManager em = emf.createEntityManager();
            try {
                List<Salle> salles = em.createQuery(
                        "SELECT s FROM Salle s WHERE s.capacite >= :capacite", Salle.class)
                        .setParameter("capacite", 30)
                        .setHint("org.hibernate.cacheable", "true")
                        .getResultList();
            } finally {
                em.close();
            }
        }
        endTime = System.currentTimeMillis();
        System.out.println("Temps d'exécution des requêtes avec cache: " + (endTime - startTime) + "ms");
    }
}
