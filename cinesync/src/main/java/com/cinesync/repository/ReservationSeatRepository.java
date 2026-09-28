package com.cinesync.repository;

import com.cinesync.model.ReservationSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Collection;
import java.util.List;

public interface ReservationSeatRepository extends JpaRepository<ReservationSeat, Long> {
    long countByShowIdAndReleasedAtIsNull(Long showId);

    @Query("select s.seatNumber from ReservationSeat s where s.show.id = :showId and s.releasedAt is null")
    List<String> findActiveSeatNumbers(@Param("showId") Long showId);

    @Query("select s.seatNumber from ReservationSeat s where s.show.id = :showId and s.releasedAt is null and s.seatNumber in :seatNumbers")
    List<String> findActiveSeatNumbers(@Param("showId") Long showId, @Param("seatNumbers") Collection<String> seatNumbers);
}
