package com.cinesync.service;

import com.cinesync.dto.ShowView;
import com.cinesync.model.Show;
import com.cinesync.repository.ReservationSeatRepository;
import com.cinesync.repository.ShowRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ShowService {
    private final ShowRepository showRepository;
    private final ReservationSeatRepository reservationSeatRepository;
    private final MovieService movieService;
    private final TheatreService theatreService;

    public ShowService(ShowRepository showRepository, ReservationSeatRepository reservationSeatRepository,
                       MovieService movieService, TheatreService theatreService) {
        this.showRepository = showRepository;
        this.reservationSeatRepository = reservationSeatRepository;
        this.movieService = movieService;
        this.theatreService = theatreService;
    }

    @Transactional(readOnly = true)
    public List<ShowView> findUpcoming() {
        return showRepository.findByShowtimeAfterOrderByShowtimeAsc(LocalDateTime.now())
                .stream().map(this::toView).toList();
    }

    @Transactional
    public Show save(Show show) {
        if (show == null || show.getMovie() == null || show.getTheatre() == null || show.getShowtime() == null) {
            throw new IllegalArgumentException("A show requires a movie, theatre, and showtime");
        }
        return showRepository.save(show);
    }

    private ShowView toView(Show show) {
        long booked = show.getId() == null ? 0 : reservationSeatRepository.findActiveSeatNumbers(show.getId()).size();
        return new ShowView(show.getId(), movieService.toView(show.getMovie()), theatreService.toView(show.getTheatre()),
                show.getShowtime(), show.getStatus(), show.getSeatCapacity(), booked);
    }
}
