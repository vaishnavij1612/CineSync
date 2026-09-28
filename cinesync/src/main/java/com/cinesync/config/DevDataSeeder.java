package com.cinesync.config;

import com.cinesync.model.Movie;
import com.cinesync.model.Show;
import com.cinesync.model.Theatre;
import com.cinesync.service.GenreService;
import com.cinesync.service.MovieService;
import com.cinesync.service.ShowService;
import com.cinesync.service.TheatreService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

@Configuration
@Profile("dev")
public class DevDataSeeder {
    @Bean
    CommandLineRunner seedDomainData(GenreService genreService, MovieService movieService,
                                     TheatreService theatreService, ShowService showService) {
        return args -> {
            Set<String> genreNames = Set.of("Action", "Comedy", "Drama", "Sci-Fi", "Thriller");
            genreNames.forEach(name -> {
                try {
                    genreService.create(name);
                } catch (IllegalArgumentException ignored) {
                    // Development seeding is idempotent when the profile is restarted.
                }
            });

            if (!movieService.findAll().isEmpty()) {
                return;
            }

            Movie dune = new Movie("Dune: Part Two", "Paul Atreides unites with Chani and the Fremen.",
                    8.7, LocalDate.of(2024, 3, 1), null);
            dune.addGenre(genreService.getByName("Sci-Fi"));
            dune.addGenre(genreService.getByName("Drama"));
            movieService.save(dune);

            Movie knivesOut = new Movie("Knives Out", "A detective investigates a family's tangled secrets.",
                    7.9, LocalDate.of(2019, 11, 27), null);
            knivesOut.addGenre(genreService.getByName("Comedy"));
            knivesOut.addGenre(genreService.getByName("Thriller"));
            movieService.save(knivesOut);

            Theatre downtown = theatreService.save(new Theatre("Downtown Screens", "12 Market Street",
                    12.9716, 77.5946, 4, 5));
            Theatre riverside = theatreService.save(new Theatre("Riverside Cinema", "8 River Road",
                    12.9352, 77.6245, 4, 5));

            showService.save(new Show(dune, downtown, LocalDateTime.now().plusDays(1).withHour(18).withMinute(30)));
            showService.save(new Show(knivesOut, riverside, LocalDateTime.now().plusDays(2).withHour(20).withMinute(0)));
        };
    }
}
