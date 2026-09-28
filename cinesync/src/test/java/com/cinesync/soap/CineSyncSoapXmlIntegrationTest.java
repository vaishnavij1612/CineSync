package com.cinesync.soap;

import com.cinesync.model.User;
import com.cinesync.service.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.ws.test.server.MockWebServiceClient;
import org.springframework.xml.transform.StringSource;

import javax.xml.transform.Source;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.ws.test.server.RequestCreators.withPayload;
import static org.springframework.ws.test.server.ResponseMatchers.*;

@SpringBootTest
class CineSyncSoapXmlIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private PreferenceService preferenceService;

    @MockitoBean
    private WatchlistService watchlistService;

    @MockitoBean
    private ReservationService reservationService;

    @MockitoBean
    private GenreService genreService;

    private MockWebServiceClient mockClient;

    private static final Map<String, String> NAMESPACES = Map.of("ns", "http://cinesync.com/soap");

    @BeforeEach
    void setUp() {
        mockClient = MockWebServiceClient.createClient(applicationContext);
    }

    @Test
    void testGetUserProfileSoapXmlSuccess() {
        User user = new User("xmluser", "xmluser@example.com", "secret", "XML User");
        when(userService.getByUsername("xmluser")).thenReturn(user);

        Source request = new StringSource(
                "<ns:getUserProfileRequest xmlns:ns=\"http://cinesync.com/soap\">" +
                "  <ns:username>xmluser</ns:username>" +
                "</ns:getUserProfileRequest>"
        );

        mockClient.sendRequest(withPayload(request))
                .andExpect(noFault())
                .andExpect(xpath("//ns:username", NAMESPACES).evaluatesTo("xmluser"))
                .andExpect(xpath("//ns:email", NAMESPACES).evaluatesTo("xmluser@example.com"))
                .andExpect(xpath("//ns:fullName", NAMESPACES).evaluatesTo("XML User"))
                .andExpect(xpath("//ns:role", NAMESPACES).evaluatesTo("USER"))
                .andExpect(xpath("//ns:enabled", NAMESPACES).evaluatesTo("true"));
    }

    @Test
    void testGetUserProfileSoapXmlUserNotFoundFault() {
        when(userService.getByUsername("unknown")).thenThrow(new IllegalArgumentException("User not found"));

        Source request = new StringSource(
                "<ns:getUserProfileRequest xmlns:ns=\"http://cinesync.com/soap\">" +
                "  <ns:username>unknown</ns:username>" +
                "</ns:getUserProfileRequest>"
        );

        mockClient.sendRequest(withPayload(request))
                .andExpect(clientOrSenderFault("USER_NOT_FOUND: User not found"));
    }

    @Test
    void testAddToWatchlistSoapXmlSuccess() {
        Source request = new StringSource(
                "<ns:addToWatchlistRequest xmlns:ns=\"http://cinesync.com/soap\">" +
                "  <ns:username>xmluser</ns:username>" +
                "  <ns:movieId>42</ns:movieId>" +
                "</ns:addToWatchlistRequest>"
        );

        mockClient.sendRequest(withPayload(request))
                .andExpect(noFault())
                .andExpect(xpath("//ns:added", NAMESPACES).evaluatesTo("true"))
                .andExpect(xpath("//ns:message", NAMESPACES).evaluatesTo("Movie added to watchlist"));
    }

    @Test
    void testCreateReservationSoapXmlInvalidSeatFault() {
        when(reservationService.createReservation(org.mockito.ArgumentMatchers.eq("xmluser"), org.mockito.ArgumentMatchers.eq(10L), org.mockito.ArgumentMatchers.anyList()))
                .thenThrow(new InvalidSeatException("Invalid seat: X99"));

        Source request = new StringSource(
                "<ns:createReservationRequest xmlns:ns=\"http://cinesync.com/soap\">" +
                "  <ns:username>xmluser</ns:username>" +
                "  <ns:showId>10</ns:showId>" +
                "  <ns:seats>X99</ns:seats>" +
                "</ns:createReservationRequest>"
        );

        mockClient.sendRequest(withPayload(request))
                .andExpect(clientOrSenderFault("INVALID_SEAT: Invalid seat: X99"));
    }
}
