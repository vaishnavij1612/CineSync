package com.cinesync.repository;

import com.cinesync.model.WatchlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface WatchlistRepository extends JpaRepository<WatchlistItem, Long> {
    List<WatchlistItem> findByUserUsernameOrderByCreatedAtDesc(String username);
    Optional<WatchlistItem> findByUserUsernameAndMovieId(String username, Long movieId);
    boolean existsByUserUsernameAndMovieId(String username, Long movieId);
    void deleteByUserUsernameAndMovieId(String username, Long movieId);
}
