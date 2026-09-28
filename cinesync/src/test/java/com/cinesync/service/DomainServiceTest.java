package com.cinesync.service;

import com.cinesync.model.Genre;
import com.cinesync.model.Movie;
import com.cinesync.model.Show;
import com.cinesync.model.Theatre;
import com.cinesync.repository.GenreRepository;
import com.cinesync.repository.MovieRepository;
import com.cinesync.repository.ReservationSeatRepository;
import com.cinesync.repository.ShowRepository;
import com.cinesync.repository.TheatreRepository;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DomainServiceTest {
    @Test
    void genreUniquenessIsCheckedBeforeSave() {
        GenreRepository repository = mock(GenreRepository.class);
        GenreService service = new GenreService(repository);
        when(repository.existsByNameIgnoreCase("drama")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> service.create(" drama "));
        verify(repository, never()).save(any());
    }

    @Test
    void movieKeepsManyToManyGenresWithoutDuplicatingNames() {
        Movie movie = new Movie("Example");
        Genre drama = new Genre("Drama");
        movie.addGenre(drama);
        movie.addGenre(drama);

        assertEquals(1, movie.getGenres().size());
        assertSame(drama, movie.getGenres().iterator().next());
    }

    @Test
    void theatreServicePersistsValidLayout() {
        TheatreRepository repository = mock(TheatreRepository.class);
        TheatreService service = new TheatreService(repository);
        Theatre theatre = new Theatre("Central", 4, 5);
        when(repository.save(theatre)).thenReturn(theatre);

        assertSame(theatre, service.save(theatre));
        verify(repository).save(theatre);
    }

    @Test
    void theatreServiceRejectsEmptyLayout() {
        TheatreRepository repository = mock(TheatreRepository.class);
        TheatreService service = new TheatreService(repository);

        assertThrows(IllegalArgumentException.class,
                () -> service.save(new Theatre("Central", 0, 5)));
        verifyNoInteractions(repository);
    }

    @Test
    void showRetainsMovieAndTheatreAssociations() {
        Movie movie = new Movie("Example");
        Theatre theatre = new Theatre("Central", 4, 5);
        Show show = new Show(movie, theatre, LocalDateTime.now().plusDays(1));
        ShowRepository repository = mock(ShowRepository.class);
        when(repository.save(show)).thenReturn(show);
        ShowService service = new ShowService(repository, mock(ReservationSeatRepository.class),
                mock(MovieService.class), mock(TheatreService.class));

        Show saved = service.save(show);

        assertSame(movie, saved.getMovie());
        assertSame(theatre, saved.getTheatre());
        verify(repository).save(show);
    }
}
