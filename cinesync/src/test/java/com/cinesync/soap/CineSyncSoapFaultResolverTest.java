package com.cinesync.soap;

import com.cinesync.service.DuplicateUserException;
import com.cinesync.service.InvalidSeatException;
import com.cinesync.service.ReservationNotFoundException;
import com.cinesync.service.SeatUnavailableException;
import com.cinesync.service.WatchlistItemNotFoundException;
import com.cinesync.soap.exception.CineSyncSoapFaultResolver;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;

import static org.assertj.core.api.Assertions.assertThat;

class CineSyncSoapFaultResolverTest {

    private final CineSyncSoapFaultResolver resolver = new CineSyncSoapFaultResolver();

    private SoapFaultDefinition getFaultDefinition(Exception ex) {
        return (SoapFaultDefinition) ReflectionTestUtils.invokeMethod(resolver, "getFaultDefinition", new Object(), ex);
    }

    @Test
    void mapsInvalidSeatException() {
        SoapFaultDefinition def = getFaultDefinition(new InvalidSeatException("Seat X1 is out of range"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("INVALID_SEAT: Seat X1 is out of range");
    }

    @Test
    void mapsSeatUnavailableException() {
        SoapFaultDefinition def = getFaultDefinition(new SeatUnavailableException("Seat A1 is booked"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("SEAT_UNAVAILABLE: Seat A1 is booked");
    }

    @Test
    void mapsReservationNotFoundException() {
        SoapFaultDefinition def = getFaultDefinition(new ReservationNotFoundException("Reservation not found"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("RESERVATION_NOT_FOUND: Reservation not found");
    }

    @Test
    void mapsWatchlistItemNotFoundException() {
        SoapFaultDefinition def = getFaultDefinition(new WatchlistItemNotFoundException("Item not found"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("WATCHLIST_ITEM_NOT_FOUND: Item not found");
    }

    @Test
    void mapsDuplicateUserException() {
        SoapFaultDefinition def = getFaultDefinition(new DuplicateUserException("Username exists"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("DUPLICATE_USER: Username exists");
    }

    @Test
    void mapsSecurityException() {
        SoapFaultDefinition def = getFaultDefinition(new SecurityException("Access denied"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("UNAUTHORIZED: Access denied");
    }

    @Test
    void mapsIllegalArgumentException_UserNotFound() {
        SoapFaultDefinition def = getFaultDefinition(new IllegalArgumentException("User not found"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("USER_NOT_FOUND: User not found");
    }

    @Test
    void mapsIllegalArgumentException_MovieNotFound() {
        SoapFaultDefinition def = getFaultDefinition(new IllegalArgumentException("Movie not found"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("MOVIE_NOT_FOUND: Movie not found");
    }

    @Test
    void mapsIllegalArgumentException_ShowNotFound() {
        SoapFaultDefinition def = getFaultDefinition(new IllegalArgumentException("Show not found"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("SHOW_NOT_FOUND: Show not found");
    }

    @Test
    void mapsIllegalArgumentException_InvalidGenre() {
        SoapFaultDefinition def = getFaultDefinition(new IllegalArgumentException("Genre not found: Anime"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("INVALID_GENRE: Genre not found: Anime");
    }

    @Test
    void mapsIllegalArgumentException_InvalidPreference() {
        SoapFaultDefinition def = getFaultDefinition(new IllegalArgumentException("Invalid showtime preference: MIDNIGHT"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("INVALID_PREFERENCE: Invalid showtime preference: MIDNIGHT");
    }

    @Test
    void mapsIllegalArgumentException_DuplicateWatchlist() {
        SoapFaultDefinition def = getFaultDefinition(new IllegalArgumentException("Movie is already in your watchlist"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("DUPLICATE_WATCHLIST_ITEM: Movie is already in your watchlist");
    }

    @Test
    void mapsIllegalStateException() {
        SoapFaultDefinition def = getFaultDefinition(new IllegalStateException("Reservation is already cancelled"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.CLIENT);
        assertThat(def.getFaultStringOrReason()).isEqualTo("INVALID_OPERATION: Reservation is already cancelled");
    }

    @Test
    void mapsGenericExceptionToServerErrorWithoutExposingStackTrace() {
        SoapFaultDefinition def = getFaultDefinition(new RuntimeException("Database connection timeout at line 42"));
        assertThat(def.getFaultCode()).isEqualTo(SoapFaultDefinition.SERVER);
        assertThat(def.getFaultStringOrReason()).isEqualTo("SERVER_ERROR: An unexpected error occurred processing the SOAP request");
    }
}
