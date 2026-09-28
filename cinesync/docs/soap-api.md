# CineSync SOAP Web Services Documentation

## Overview
CineSync provides a SOAP 1.1 Web Service interface powered by Spring Web Services. It allows external clients to interact with CineSync core features including user profiles, preferences, watchlists, and show reservations.

---

## Service Specifications

* **Endpoint Location:** `http://localhost:8080/ws`
* **WSDL URL:** `http://localhost:8080/ws/cinesync.wsdl`
* **Target Namespace:** `http://cinesync.com/soap`
* **Port Type:** `CineSyncPort`
* **Protocol:** SOAP 1.1 Document/Literal (wrapped)

---

## Authentication & Ownership Strategy

1. **Identity via Request Payload:** Since SOAP interactions are stateless and non-browser driven (avoiding session cookies/CSRF), client requests specify the target `username` parameter.
2. **Service-Layer Authorization:** All business logic operations delegate to existing CineSync domain services (`UserService`, `PreferenceService`, `WatchlistService`, `ReservationService`).
3. **Owner Enforcement:** Services strictly enforce user ownership. For instance:
   * Cancellation validates that the reservation belongs to the specified user.
   * Watchlist operations query and mutate only the specified user's items.
   * Reservations list retrieves only the authenticated user's records.
4. **Credential Safety:** Plain passwords, password hashes, and sensitive authentication secrets are **never accepted** or **returned** via SOAP operations.

---

## Available SOAP Operations

| # | Operation | Request Element | Response Element | Description |
|---|---|---|---|---|
| 1 | `getUserProfile` | `getUserProfileRequest` | `getUserProfileResponse` | Retrieves user profile metadata (ID, email, name, role, status). |
| 2 | `updateUserPreferences` | `updateUserPreferencesRequest` | `updateUserPreferencesResponse` | Updates preferred genres and showtime category. |
| 3 | `addToWatchlist` | `addToWatchlistRequest` | `addToWatchlistResponse` | Adds a movie to the user's personal watchlist. |
| 4 | `removeFromWatchlist` | `removeFromWatchlistRequest` | `removeFromWatchlistResponse` | Removes a movie from the user's personal watchlist. |
| 5 | `getWatchlist` | `getWatchlistRequest` | `getWatchlistResponse` | Returns all movies currently in user's watchlist. |
| 6 | `createReservation` | `createReservationRequest` | `createReservationResponse` | Reserves specific seats for a show. |
| 7 | `cancelReservation` | `cancelReservationRequest` | `cancelReservationResponse` | Cancels an active confirmed reservation. |
| 8 | `getReservations` | `getReservationsRequest` | `getReservationsResponse` | Retrieves all reservations made by the user. |

---

## Sample Request & Response Payloads

### 1. `getUserProfile`

**Request:**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:soap="http://cinesync.com/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:getUserProfileRequest>
         <soap:username>john_doe</soap:username>
      </soap:getUserProfileRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

**Response:**
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header/>
   <SOAP-ENV:Body>
      <ns2:getUserProfileResponse xmlns:ns2="http://cinesync.com/soap">
         <ns2:userId>42</ns2:userId>
         <ns2:username>john_doe</ns2:username>
         <ns2:email>john@example.com</ns2:email>
         <ns2:fullName>John Doe</ns2:fullName>
         <ns2:role>USER</ns2:role>
         <ns2:enabled>true</ns2:enabled>
      </ns2:getUserProfileResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

---

### 2. `updateUserPreferences`

**Request:**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:soap="http://cinesync.com/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:updateUserPreferencesRequest>
         <soap:username>john_doe</soap:username>
         <soap:genres>Action, Sci-Fi</soap:genres>
         <soap:preferredShowtime>EVENING</soap:preferredShowtime>
      </soap:updateUserPreferencesRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

**Response:**
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header/>
   <SOAP-ENV:Body>
      <ns2:updateUserPreferencesResponse xmlns:ns2="http://cinesync.com/soap">
         <ns2:updated>true</ns2:updated>
         <ns2:message>User preferences updated successfully</ns2:message>
      </ns2:updateUserPreferencesResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

---

### 3. `createReservation`

**Request:**
```xml
<soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                  xmlns:soap="http://cinesync.com/soap">
   <soapenv:Header/>
   <soapenv:Body>
      <soap:createReservationRequest>
         <soap:username>john_doe</soap:username>
         <soap:showId>5</soap:showId>
         <soap:seats>A1</soap:seats>
         <soap:seats>A2</soap:seats>
      </soap:createReservationRequest>
   </soapenv:Body>
</soapenv:Envelope>
```

**Response:**
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header/>
   <SOAP-ENV:Body>
      <ns2:createReservationResponse xmlns:ns2="http://cinesync.com/soap">
         <ns2:reservationId>108</ns2:reservationId>
         <ns2:showId>5</ns2:showId>
         <ns2:movieTitle>Inception</ns2:movieTitle>
         <ns2:theatreName>Grand Cinema</ns2:theatreName>
         <ns2:showtime>2026-09-30T18:00:00</ns2:showtime>
         <ns2:seats>A1</ns2:seats>
         <ns2:seats>A2</ns2:seats>
         <ns2:status>CONFIRMED</ns2:status>
         <ns2:createdAt>2026-09-25T13:00:00</ns2:createdAt>
      </ns2:createReservationResponse>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```

---

## SOAP Fault Handling

Errors are mapped directly to standard SOAP 1.1 Fault envelopes using `CineSyncSoapFaultResolver`. Java stack traces are **never** exposed.

### Common Fault Codes & Strings

| Fault Code | Fault String Format | Cause |
|---|---|---|
| `SOAP-ENV:Client` | `USER_NOT_FOUND: User not found` | Username is not registered. |
| `SOAP-ENV:Client` | `MOVIE_NOT_FOUND: Movie not found` | Movie ID does not exist. |
| `SOAP-ENV:Client` | `SHOW_NOT_FOUND: Show not found` | Show ID does not exist. |
| `SOAP-ENV:Client` | `INVALID_SEAT: ...` | Requested seat name/number is invalid for the theatre layout. |
| `SOAP-ENV:Client` | `SEAT_UNAVAILABLE: ...` | Requested seat is already reserved for the show. |
| `SOAP-ENV:Client` | `RESERVATION_NOT_FOUND: ...` | Reservation does not exist or does not belong to user. |
| `SOAP-ENV:Client` | `INVALID_PREFERENCE: ...` | Invalid showtime enum value (valid: ANY, MORNING, AFTERNOON, EVENING, NIGHT). |
| `SOAP-ENV:Client` | `INVALID_GENRE: ...` | Genre name or ID does not exist. |
| `SOAP-ENV:Client` | `DUPLICATE_WATCHLIST_ITEM: ...` | Movie is already in user's watchlist. |
| `SOAP-ENV:Server` | `SERVER_ERROR: An unexpected error...` | Internal system error. |

**Example Fault Response:**
```xml
<SOAP-ENV:Envelope xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/">
   <SOAP-ENV:Header/>
   <SOAP-ENV:Body>
      <SOAP-ENV:Fault>
         <faultcode>SOAP-ENV:Client</faultcode>
         <faultstring xml:lang="en">USER_NOT_FOUND: User not found</faultstring>
      </SOAP-ENV:Fault>
   </SOAP-ENV:Body>
</SOAP-ENV:Envelope>
```
