package com.cinesync.soap;

import com.cinesync.dto.MovieView;
import com.cinesync.dto.PreferenceForm;
import com.cinesync.dto.PreferenceView;
import com.cinesync.dto.WatchlistView;
import com.cinesync.model.*;
import com.cinesync.service.*;
import com.cinesync.soap.dto.*;
import com.cinesync.soap.endpoint.CineSyncSoapEndpoint;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CineSyncSoapEndpointTest {

    @Mock
    private UserService userService;
    @Mock
    private PreferenceService preferenceService;
    @Mock
    private WatchlistService watchlistService;
    @Mock
    private ReservationService reservationService;
    @Mock
    private GenreService genreService;

    private CineSyncSoapEndpoint endpoint;

    @BeforeEach
    void setUp() {
        endpoint = new CineSyncSoapEndpoint(
                userService,
                preferenceService,
                watchlistService,
                reservationService,
                genreService
        );
    }

    // ─────────────────────────────────────────────────────────────────
    // 1. getUserProfile
    // ─────────────────────────────────────────────────────────────────
    @Test
    void getUserProfile_Success() {
        User user = new User("alice", "alice@example.com", "secretHash", "Alice Smith");
        ReflectionTestUtils.setField(user, "id", 101L);
        when(userService.getByUsername("alice")).thenReturn(user);

        GetUserProfileRequest request = new GetUserProfileRequest("alice");
        GetUserProfileResponse response = endpoint.getUserProfile(request);

        assertThat(response).isNotNull();
        assertThat(response.getUserId()).isEqualTo(101L);
        assertThat(response.getUsername()).isEqualTo("alice");
        assertThat(response.getEmail()).isEqualTo("alice@example.com");
        assertThat(response.getFullName()).isEqualTo("Alice Smith");
        assertThat(response.getRole()).isEqualTo("USER");
        assertThat(response.isEnabled()).isTrue();
        // Crucial security verification: password must not be in response
        verify(userService).getByUsername("alice");
    }

    @Test
    void getUserProfile_UserNotFound() {
        when(userService.getByUsername("ghost")).thenThrow(new IllegalArgumentException("User not found"));

        GetUserProfileRequest request = new GetUserProfileRequest("ghost");
        assertThatThrownBy(() -> endpoint.getUserProfile(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    @Test
    void getUserProfile_NullOrBlankUsername() {
        assertThatThrownBy(() -> endpoint.getUserProfile(new GetUserProfileRequest(null)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");

        assertThatThrownBy(() -> endpoint.getUserProfile(new GetUserProfileRequest("   ")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found");
    }

    // ─────────────────────────────────────────────────────────────────
    // 2. updateUserPreferences
    // ─────────────────────────────────────────────────────────────────
    @Test
    void updateUserPreferences_WithGenreIds_Success() {
        when(preferenceService.update(eq("alice"), any(PreferenceForm.class)))
                .thenReturn(new PreferenceView(List.of(1L, 2L), List.of("Action", "Drama"), ShowtimePreference.EVENING));

        UpdateUserPreferencesRequest request = new UpdateUserPreferencesRequest("alice", List.of(1L, 2L), "EVENING");
        UpdateUserPreferencesResponse response = endpoint.updateUserPreferences(request);

        assertThat(response).isNotNull();
        assertThat(response.isUpdated()).isTrue();
        assertThat(response.getMessage()).contains("successfully");

        ArgumentCaptor<PreferenceForm> captor = ArgumentCaptor.forClass(PreferenceForm.class);
        verify(preferenceService).update(eq("alice"), captor.capture());
        assertThat(captor.getValue().getGenreIds()).containsExactly(1L, 2L);
        assertThat(captor.getValue().getShowtimePreference()).isEqualTo(ShowtimePreference.EVENING);
    }

    @Test
    void updateUserPreferences_WithGenreNames_Success() {
        Genre action = new Genre("Action");
        ReflectionTestUtils.setField(action, "id", 10L);
        Genre comedy = new Genre("Comedy");
        ReflectionTestUtils.setField(comedy, "id", 20L);

        when(genreService.getByName("Action")).thenReturn(action);
        when(genreService.getByName("Comedy")).thenReturn(comedy);
        when(preferenceService.update(eq("alice"), any(PreferenceForm.class)))
                .thenReturn(new PreferenceView(List.of(10L, 20L), List.of("Action", "Comedy"), ShowtimePreference.NIGHT));

        UpdateUserPreferencesRequest request = new UpdateUserPreferencesRequest("alice", "Action, Comedy", "NIGHT");
        UpdateUserPreferencesResponse response = endpoint.updateUserPreferences(request);

        assertThat(response.isUpdated()).isTrue();
        ArgumentCaptor<PreferenceForm> captor = ArgumentCaptor.forClass(PreferenceForm.class);
        verify(preferenceService).update(eq("alice"), captor.capture());
        assertThat(captor.getValue().getGenreIds()).containsExactly(10L, 20L);
        assertThat(captor.getValue().getShowtimePreference()).isEqualTo(ShowtimePreference.NIGHT);
    }

    @Test
    void updateUserPreferences_InvalidShowtime() {
        UpdateUserPreferencesRequest request = new UpdateUserPreferencesRequest("alice", List.of(1L), "MIDNIGHT");

        assertThatThrownBy(() -> endpoint.updateUserPreferences(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Invalid showtime preference: MIDNIGHT");
    }

    @Test
    void updateUserPreferences_InvalidGenreName() {
        when(genreService.getByName("SciFi")).thenThrow(new IllegalArgumentException("Genre not found: SciFi"));

        UpdateUserPreferencesRequest request = new UpdateUserPreferencesRequest("alice", "SciFi", "ANY");

        assertThatThrownBy(() -> endpoint.updateUserPreferences(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Genre not found: SciFi");
    }

    // ─────────────────────────────────────────────────────────────────
    // 3. addToWatchlist
    // ─────────────────────────────────────────────────────────────────
    @Test
    void addToWatchlist_Success() {
        User user = new User("alice", "alice@example.com", "pass", "Alice");
        Movie movie = new Movie("Inception");
        ReflectionTestUtils.setField(movie, "id", 50L);
        WatchlistItem item = new WatchlistItem(user, movie);

        when(watchlistService.addMovieToWatchlist("alice", 50L)).thenReturn(item);

        AddToWatchlistRequest request = new AddToWatchlistRequest("alice", 50L);
        AddToWatchlistResponse response = endpoint.addToWatchlist(request);

        assertThat(response).isNotNull();
        assertThat(response.isAdded()).isTrue();
        assertThat(response.getMessage()).contains("added to watchlist");
        verify(watchlistService).addMovieToWatchlist("alice", 50L);
    }

    @Test
    void addToWatchlist_DuplicateItem() {
        when(watchlistService.addMovieToWatchlist("alice", 50L))
                .thenThrow(new IllegalArgumentException("Movie is already in your watchlist"));

        AddToWatchlistRequest request = new AddToWatchlistRequest("alice", 50L);

        assertThatThrownBy(() -> endpoint.addToWatchlist(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already in your watchlist");
    }

    @Test
    void addToWatchlist_MovieNotFound() {
        when(watchlistService.addMovieToWatchlist("alice", 999L))
                .thenThrow(new IllegalArgumentException("Movie not found"));

        AddToWatchlistRequest request = new AddToWatchlistRequest("alice", 999L);

        assertThatThrownBy(() -> endpoint.addToWatchlist(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Movie not found");
    }

    // ─────────────────────────────────────────────────────────────────
    // 4. removeFromWatchlist
    // ─────────────────────────────────────────────────────────────────
    @Test
    void removeFromWatchlist_Success() {
        doNothing().when(watchlistService).removeMovieFromWatchlist("alice", 50L);

        RemoveFromWatchlistRequest request = new RemoveFromWatchlistRequest("alice", 50L);
        RemoveFromWatchlistResponse response = endpoint.removeFromWatchlist(request);

        assertThat(response).isNotNull();
        assertThat(response.isRemoved()).isTrue();
        verify(watchlistService).removeMovieFromWatchlist("alice", 50L);
    }

    @Test
    void removeFromWatchlist_NotFound() {
        doThrow(new WatchlistItemNotFoundException("Movie is not in your watchlist"))
                .when(watchlistService).removeMovieFromWatchlist("alice", 99L);

        RemoveFromWatchlistRequest request = new RemoveFromWatchlistRequest("alice", 99L);

        assertThatThrownBy(() -> endpoint.removeFromWatchlist(request))
                .isInstanceOf(WatchlistItemNotFoundException.class)
                .hasMessageContaining("Movie is not in your watchlist");
    }

    // ─────────────────────────────────────────────────────────────────
    // 5. getWatchlist
    // ─────────────────────────────────────────────────────────────────
    @Test
    void getWatchlist_Success() {
        MovieView mv1 = new MovieView(1L, "Inception", "Mind-bending", 8.8,
                LocalDate.of(2010, 7, 16), "/poster1.jpg", List.of("Sci-Fi", "Action"));
        MovieView mv2 = new MovieView(2L, "Interstellar", "Space travel", 8.6,
                LocalDate.of(2014, 11, 7), "/poster2.jpg", List.of("Sci-Fi", "Drama"));

        WatchlistView wv1 = new WatchlistView(mv1, LocalDateTime.of(2026, 1, 10, 12, 0));
        WatchlistView wv2 = new WatchlistView(mv2, LocalDateTime.of(2026, 1, 12, 15, 30));

        when(watchlistService.getCurrentUserWatchlist("alice")).thenReturn(List.of(wv1, wv2));

        GetWatchlistRequest request = new GetWatchlistRequest("alice");
        GetWatchlistResponse response = endpoint.getWatchlist(request);

        assertThat(response).isNotNull();
        assertThat(response.getItems()).hasSize(2);
        assertThat(response.getItems().get(0).getTitle()).isEqualTo("Inception");
        assertThat(response.getItems().get(0).getGenres()).isEqualTo("Sci-Fi, Action");
        assertThat(response.getItems().get(0).getRating()).isEqualTo(8.8);
        assertThat(response.getItems().get(1).getTitle()).isEqualTo("Interstellar");
    }

    // ─────────────────────────────────────────────────────────────────
    // 6. createReservation
    // ─────────────────────────────────────────────────────────────────
    @Test
    void createReservation_Success() {
        User user = new User("alice", "alice@example.com", "pass", "Alice");
        Theatre theatre = new Theatre("Grand Cinema", "123 Main St", 12.9, 77.5, 4, 5);
        Movie movie = new Movie("Dune 2");
        Show show = new Show(movie, theatre, LocalDateTime.of(2026, 9, 30, 18, 0));
        ReflectionTestUtils.setField(show, "id", 200L);

        Reservation reservation = new Reservation(user, show);
        ReflectionTestUtils.setField(reservation, "id", 500L);
        reservation.addSeat(new ReservationSeat(show, "A1"));
        reservation.addSeat(new ReservationSeat(show, "A2"));

        when(reservationService.createReservation("alice", 200L, List.of("A1", "A2")))
                .thenReturn(reservation);

        CreateReservationRequest request = new CreateReservationRequest("alice", 200L, List.of("A1", "A2"));
        CreateReservationResponse response = endpoint.createReservation(request);

        assertThat(response).isNotNull();
        assertThat(response.getReservationId()).isEqualTo(500L);
        assertThat(response.getShowId()).isEqualTo(200L);
        assertThat(response.getMovieTitle()).isEqualTo("Dune 2");
        assertThat(response.getTheatreName()).isEqualTo("Grand Cinema");
        assertThat(response.getSeats()).containsExactly("A1", "A2");
        assertThat(response.getStatus()).isEqualTo("CONFIRMED");
    }

    @Test
    void createReservation_InvalidSeat() {
        when(reservationService.createReservation("alice", 200L, List.of("Z99")))
                .thenThrow(new InvalidSeatException("Invalid seat: Z99"));

        CreateReservationRequest request = new CreateReservationRequest("alice", 200L, List.of("Z99"));

        assertThatThrownBy(() -> endpoint.createReservation(request))
                .isInstanceOf(InvalidSeatException.class)
                .hasMessageContaining("Invalid seat: Z99");
    }

    @Test
    void createReservation_SeatUnavailable() {
        when(reservationService.createReservation("alice", 200L, List.of("A1")))
                .thenThrow(new SeatUnavailableException("One or more requested seats are unavailable"));

        CreateReservationRequest request = new CreateReservationRequest("alice", 200L, List.of("A1"));

        assertThatThrownBy(() -> endpoint.createReservation(request))
                .isInstanceOf(SeatUnavailableException.class)
                .hasMessageContaining("unavailable");
    }

    // ─────────────────────────────────────────────────────────────────
    // 7. cancelReservation
    // ─────────────────────────────────────────────────────────────────
    @Test
    void cancelReservation_Success() {
        doNothing().when(reservationService).cancelReservation("alice", 500L);

        CancelReservationRequest request = new CancelReservationRequest("alice", 500L);
        CancelReservationResponse response = endpoint.cancelReservation(request);

        assertThat(response).isNotNull();
        assertThat(response.isCancelled()).isTrue();
        assertThat(response.getMessage()).contains("cancelled successfully");
        verify(reservationService).cancelReservation("alice", 500L);
    }

    @Test
    void cancelReservation_UnauthorizedOrNotFound() {
        doThrow(new ReservationNotFoundException("Reservation not found"))
                .when(reservationService).cancelReservation("bob", 500L);

        CancelReservationRequest request = new CancelReservationRequest("bob", 500L);

        assertThatThrownBy(() -> endpoint.cancelReservation(request))
                .isInstanceOf(ReservationNotFoundException.class)
                .hasMessageContaining("Reservation not found");
    }

    @Test
    void cancelReservation_AlreadyCancelled() {
        doThrow(new IllegalStateException("Reservation is already cancelled"))
                .when(reservationService).cancelReservation("alice", 500L);

        CancelReservationRequest request = new CancelReservationRequest("alice", 500L);

        assertThatThrownBy(() -> endpoint.cancelReservation(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already cancelled");
    }

    // ─────────────────────────────────────────────────────────────────
    // 8. getReservations
    // ─────────────────────────────────────────────────────────────────
    @Test
    void getReservations_Success() {
        User user = new User("alice", "alice@example.com", "pass", "Alice");
        Theatre theatre = new Theatre("Grand Cinema", "123 Main St", 12.9, 77.5, 4, 5);
        Movie movie = new Movie("Dune 2");
        Show show = new Show(movie, theatre, LocalDateTime.of(2026, 9, 30, 18, 0));
        ReflectionTestUtils.setField(show, "id", 200L);

        Reservation r1 = new Reservation(user, show);
        ReflectionTestUtils.setField(r1, "id", 501L);
        r1.addSeat(new ReservationSeat(show, "B1"));

        Reservation r2 = new Reservation(user, show);
        ReflectionTestUtils.setField(r2, "id", 502L);
        r2.addSeat(new ReservationSeat(show, "B2"));
        r2.cancel();

        when(reservationService.getUserReservations("alice")).thenReturn(List.of(r1, r2));

        GetReservationsRequest request = new GetReservationsRequest("alice");
        GetReservationsResponse response = endpoint.getReservations(request);

        assertThat(response).isNotNull();
        assertThat(response.getReservations()).hasSize(2);
        assertThat(response.getReservations().get(0).getReservationId()).isEqualTo(501L);
        assertThat(response.getReservations().get(0).getStatus()).isEqualTo("CONFIRMED");
        assertThat(response.getReservations().get(0).getSeats()).containsExactly("B1");

        assertThat(response.getReservations().get(1).getReservationId()).isEqualTo(502L);
        assertThat(response.getReservations().get(1).getStatus()).isEqualTo("CANCELLED");
        assertThat(response.getReservations().get(1).getCancelledAt()).isNotNull();
    }
}
