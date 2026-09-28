package com.cinesync.soap.endpoint;

import com.cinesync.dto.PreferenceForm;
import com.cinesync.dto.WatchlistView;
import com.cinesync.model.*;
import com.cinesync.service.*;
import com.cinesync.soap.dto.*;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.*;

@Endpoint
public class CineSyncSoapEndpoint {
    public static final String NAMESPACE_URI = "http://cinesync.com/soap";

    private final UserService userService;
    private final PreferenceService preferenceService;
    private final WatchlistService watchlistService;
    private final ReservationService reservationService;
    private final GenreService genreService;

    public CineSyncSoapEndpoint(UserService userService,
                                PreferenceService preferenceService,
                                WatchlistService watchlistService,
                                ReservationService reservationService,
                                GenreService genreService) {
        this.userService = userService;
        this.preferenceService = preferenceService;
        this.watchlistService = watchlistService;
        this.reservationService = reservationService;
        this.genreService = genreService;
    }

    // =========================================================================
    // 1. getUserProfile
    // =========================================================================
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUserProfileRequest")
    @ResponsePayload
    public GetUserProfileResponse getUserProfile(@RequestPayload GetUserProfileRequest request) {
        validateUsername(request != null ? request.getUsername() : null);
        User user = userService.getByUsername(request.getUsername());
        return new GetUserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole(),
                user.isEnabled()
        );
    }

    // =========================================================================
    // 2. updateUserPreferences
    // =========================================================================
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "updateUserPreferencesRequest")
    @ResponsePayload
    public UpdateUserPreferencesResponse updateUserPreferences(@RequestPayload UpdateUserPreferencesRequest request) {
        validateUsername(request != null ? request.getUsername() : null);

        PreferenceForm form = new PreferenceForm();
        Set<Long> genreIds = new LinkedHashSet<>();

        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            genreIds.addAll(request.getGenreIds());
        } else if (request.getGenres() != null && !request.getGenres().isBlank()) {
            String[] tokens = request.getGenres().split("[,;|]");
            for (String token : tokens) {
                String clean = token.trim();
                if (!clean.isEmpty()) {
                    try {
                        genreIds.add(Long.parseLong(clean));
                    } catch (NumberFormatException e) {
                        Genre g = genreService.getByName(clean);
                        genreIds.add(g.getId());
                    }
                }
            }
        }
        form.setGenreIds(genreIds);

        if (request.getPreferredShowtime() != null && !request.getPreferredShowtime().isBlank()) {
            try {
                ShowtimePreference pref = ShowtimePreference.valueOf(request.getPreferredShowtime().trim().toUpperCase(Locale.ROOT));
                form.setShowtimePreference(pref);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid showtime preference: " + request.getPreferredShowtime());
            }
        } else {
            form.setShowtimePreference(ShowtimePreference.ANY);
        }

        preferenceService.update(request.getUsername(), form);
        return new UpdateUserPreferencesResponse(true, "User preferences updated successfully");
    }

    // =========================================================================
    // 3. addToWatchlist
    // =========================================================================
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "addToWatchlistRequest")
    @ResponsePayload
    public AddToWatchlistResponse addToWatchlist(@RequestPayload AddToWatchlistRequest request) {
        validateUsername(request != null ? request.getUsername() : null);
        if (request.getMovieId() == null) {
            throw new IllegalArgumentException("Movie ID is required");
        }
        watchlistService.addMovieToWatchlist(request.getUsername(), request.getMovieId());
        return new AddToWatchlistResponse(true, "Movie added to watchlist");
    }

    // =========================================================================
    // 4. removeFromWatchlist
    // =========================================================================
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "removeFromWatchlistRequest")
    @ResponsePayload
    public RemoveFromWatchlistResponse removeFromWatchlist(@RequestPayload RemoveFromWatchlistRequest request) {
        validateUsername(request != null ? request.getUsername() : null);
        if (request.getMovieId() == null) {
            throw new IllegalArgumentException("Movie ID is required");
        }
        watchlistService.removeMovieFromWatchlist(request.getUsername(), request.getMovieId());
        return new RemoveFromWatchlistResponse(true, "Movie removed from watchlist");
    }

    // =========================================================================
    // 5. getWatchlist
    // =========================================================================
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getWatchlistRequest")
    @ResponsePayload
    public GetWatchlistResponse getWatchlist(@RequestPayload GetWatchlistRequest request) {
        validateUsername(request != null ? request.getUsername() : null);
        List<WatchlistView> items = watchlistService.getCurrentUserWatchlist(request.getUsername());
        List<WatchlistItemSoapDto> dtos = items.stream().map(view -> new WatchlistItemSoapDto(
                view.movie().id(),
                view.movie().title(),
                view.movie().overview(),
                view.movie().rating(),
                view.movie().posterUrl(),
                view.movie().genres() != null ? String.join(", ", view.movie().genres()) : null,
                view.addedAt() != null ? view.addedAt().toString() : null
        )).toList();
        return new GetWatchlistResponse(dtos);
    }

    // =========================================================================
    // 6. createReservation
    // =========================================================================
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "createReservationRequest")
    @ResponsePayload
    public CreateReservationResponse createReservation(@RequestPayload CreateReservationRequest request) {
        validateUsername(request != null ? request.getUsername() : null);
        if (request.getShowId() == null) {
            throw new IllegalArgumentException("Show ID is required");
        }
        if (request.getSeats() == null || request.getSeats().isEmpty()) {
            throw new InvalidSeatException("At least one seat is required");
        }

        Reservation reservation = reservationService.createReservation(
                request.getUsername(),
                request.getShowId(),
                request.getSeats()
        );

        List<String> seatNumbers = reservation.getSeats().stream()
                .map(ReservationSeat::getSeatNumber)
                .toList();

        return new CreateReservationResponse(
                reservation.getId(),
                reservation.getShow().getId(),
                reservation.getShow().getMovie().getTitle(),
                reservation.getShow().getTheatre().getName(),
                reservation.getShow().getShowtime().toString(),
                seatNumbers,
                reservation.getStatus().name(),
                reservation.getCreatedAt().toString()
        );
    }

    // =========================================================================
    // 7. cancelReservation
    // =========================================================================
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "cancelReservationRequest")
    @ResponsePayload
    public CancelReservationResponse cancelReservation(@RequestPayload CancelReservationRequest request) {
        validateUsername(request != null ? request.getUsername() : null);
        if (request.getReservationId() == null) {
            throw new IllegalArgumentException("Reservation ID is required");
        }
        reservationService.cancelReservation(request.getUsername(), request.getReservationId());
        return new CancelReservationResponse(true, "Reservation cancelled successfully");
    }

    // =========================================================================
    // 8. getReservations
    // =========================================================================
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getReservationsRequest")
    @ResponsePayload
    public GetReservationsResponse getReservations(@RequestPayload GetReservationsRequest request) {
        validateUsername(request != null ? request.getUsername() : null);
        List<Reservation> list = reservationService.getUserReservations(request.getUsername());
        List<ReservationSoapDto> dtos = list.stream().map(r -> new ReservationSoapDto(
                r.getId(),
                r.getShow().getId(),
                r.getShow().getMovie().getTitle(),
                r.getShow().getTheatre().getName(),
                r.getShow().getShowtime().toString(),
                r.getSeats().stream().map(ReservationSeat::getSeatNumber).toList(),
                r.getStatus().name(),
                r.getCreatedAt().toString(),
                r.getCancelledAt() != null ? r.getCancelledAt().toString() : null
        )).toList();
        return new GetReservationsResponse(dtos);
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("User not found");
        }
    }
}
