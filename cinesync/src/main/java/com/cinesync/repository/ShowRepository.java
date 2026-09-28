package com.cinesync.repository;

import com.cinesync.model.Show;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ShowRepository extends JpaRepository<Show, Long> {
	List<Show> findByShowtimeAfterOrderByShowtimeAsc(LocalDateTime time);
}
