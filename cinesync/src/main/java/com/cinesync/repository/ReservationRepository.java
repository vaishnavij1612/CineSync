package com.cinesync.repository;

import com.cinesync.model.Reservation;
import com.cinesync.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserIdOrderByCreatedAtDesc(Long userId);
    java.util.Optional<Reservation> findByIdAndUserId(Long id, Long userId);
    long countByShowIdAndStatus(Long showId, ReservationStatus status);
}
