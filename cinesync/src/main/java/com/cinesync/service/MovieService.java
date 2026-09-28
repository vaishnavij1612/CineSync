package com.cinesync.service;

import com.cinesync.dto.MovieView;
import com.cinesync.model.Movie;
import com.cinesync.repository.MovieRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    @Transactional(readOnly = true)
    public List<MovieView> findAll() {
        return movieRepository.findAll(org.springframework.data.domain.Sort.by("title").ascending())
                .stream().map(this::toView).toList();
    }

    @Transactional
    public Movie save(Movie movie) {
        if (movie == null || movie.getTitle() == null || movie.getTitle().isBlank()) {
            throw new IllegalArgumentException("Movie title is required");
        }
        return movieRepository.save(movie);
    }

    public MovieView toView(Movie movie) {
        return new MovieView(movie.getId(), movie.getTitle(), movie.getOverview(), movie.getRating(),
                movie.getReleaseDate(), movie.getPosterUrl(),
                movie.getGenres().stream().map(genre -> genre.getName()).toList());
    }
}
