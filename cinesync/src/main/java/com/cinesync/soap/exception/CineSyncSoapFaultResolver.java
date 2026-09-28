package com.cinesync.soap.exception;

import com.cinesync.service.DuplicateUserException;
import com.cinesync.service.InvalidSeatException;
import com.cinesync.service.ReservationNotFoundException;
import com.cinesync.service.SeatUnavailableException;
import com.cinesync.service.WatchlistItemNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.ws.soap.server.endpoint.AbstractSoapFaultDefinitionExceptionResolver;
import org.springframework.ws.soap.server.endpoint.SoapFaultDefinition;

import javax.xml.namespace.QName;

@Component
public class CineSyncSoapFaultResolver extends AbstractSoapFaultDefinitionExceptionResolver {

    public CineSyncSoapFaultResolver() {
        // Order before default resolvers so our custom fault codes take precedence
        setOrder(1);
    }

    @Override
    protected SoapFaultDefinition getFaultDefinition(Object endpoint, Exception ex) {
        SoapFaultDefinition faultDefinition = new SoapFaultDefinition();
        faultDefinition.setFaultCode(SoapFaultDefinition.CLIENT);

        if (ex instanceof InvalidSeatException) {
            faultDefinition.setFaultStringOrReason("INVALID_SEAT: " + ex.getMessage());
            return faultDefinition;
        }
        if (ex instanceof SeatUnavailableException) {
            faultDefinition.setFaultStringOrReason("SEAT_UNAVAILABLE: " + ex.getMessage());
            return faultDefinition;
        }
        if (ex instanceof ReservationNotFoundException) {
            faultDefinition.setFaultStringOrReason("RESERVATION_NOT_FOUND: " + ex.getMessage());
            return faultDefinition;
        }
        if (ex instanceof WatchlistItemNotFoundException) {
            faultDefinition.setFaultStringOrReason("WATCHLIST_ITEM_NOT_FOUND: " + ex.getMessage());
            return faultDefinition;
        }
        if (ex instanceof DuplicateUserException) {
            faultDefinition.setFaultStringOrReason("DUPLICATE_USER: " + ex.getMessage());
            return faultDefinition;
        }
        if (ex instanceof SecurityException) {
            faultDefinition.setFaultStringOrReason("UNAUTHORIZED: " + ex.getMessage());
            return faultDefinition;
        }
        if (ex instanceof IllegalArgumentException) {
            String msg = ex.getMessage() == null ? "Invalid argument" : ex.getMessage();
            if (msg.toLowerCase().contains("user not found")) {
                faultDefinition.setFaultStringOrReason("USER_NOT_FOUND: " + msg);
            } else if (msg.toLowerCase().contains("movie not found")) {
                faultDefinition.setFaultStringOrReason("MOVIE_NOT_FOUND: " + msg);
            } else if (msg.toLowerCase().contains("show not found")) {
                faultDefinition.setFaultStringOrReason("SHOW_NOT_FOUND: " + msg);
            } else if (msg.toLowerCase().contains("genre")) {
                faultDefinition.setFaultStringOrReason("INVALID_GENRE: " + msg);
            } else if (msg.toLowerCase().contains("showtime")) {
                faultDefinition.setFaultStringOrReason("INVALID_PREFERENCE: " + msg);
            } else if (msg.toLowerCase().contains("watchlist")) {
                faultDefinition.setFaultStringOrReason("DUPLICATE_WATCHLIST_ITEM: " + msg);
            } else {
                faultDefinition.setFaultStringOrReason("VALIDATION_ERROR: " + msg);
            }
            return faultDefinition;
        }
        if (ex instanceof IllegalStateException) {
            faultDefinition.setFaultStringOrReason("INVALID_OPERATION: " + ex.getMessage());
            return faultDefinition;
        }

        // Fallback for unexpected server exceptions - hide internal stack trace
        faultDefinition.setFaultCode(SoapFaultDefinition.SERVER);
        faultDefinition.setFaultStringOrReason("SERVER_ERROR: An unexpected error occurred processing the SOAP request");
        return faultDefinition;
    }
}
