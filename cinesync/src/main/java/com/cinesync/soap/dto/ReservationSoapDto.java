package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import java.util.ArrayList;
import java.util.List;

@XmlType(name = "reservationSoapDto", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class ReservationSoapDto {
    @XmlElement(required = true)
    private Long reservationId;

    @XmlElement(required = true)
    private Long showId;

    @XmlElement(required = true)
    private String movieTitle;

    @XmlElement(required = true)
    private String theatreName;

    @XmlElement(required = true)
    private String showtime;

    @XmlElement(name = "seats")
    private List<String> seats = new ArrayList<>();

    @XmlElement(required = true)
    private String status;

    @XmlElement(required = true)
    private String createdAt;

    private String cancelledAt;

    public ReservationSoapDto() {}

    public ReservationSoapDto(Long reservationId, Long showId, String movieTitle,
                              String theatreName, String showtime, List<String> seats,
                              String status, String createdAt, String cancelledAt) {
        this.reservationId = reservationId;
        this.showId = showId;
        this.movieTitle = movieTitle;
        this.theatreName = theatreName;
        this.showtime = showtime;
        this.seats = seats != null ? seats : new ArrayList<>();
        this.status = status;
        this.createdAt = createdAt;
        this.cancelledAt = cancelledAt;
    }

    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
    public Long getShowId() { return showId; }
    public void setShowId(Long showId) { this.showId = showId; }
    public String getMovieTitle() { return movieTitle; }
    public void setMovieTitle(String movieTitle) { this.movieTitle = movieTitle; }
    public String getTheatreName() { return theatreName; }
    public void setTheatreName(String theatreName) { this.theatreName = theatreName; }
    public String getShowtime() { return showtime; }
    public void setShowtime(String showtime) { this.showtime = showtime; }
    public List<String> getSeats() { return seats; }
    public void setSeats(List<String> seats) { this.seats = seats; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(String cancelledAt) { this.cancelledAt = cancelledAt; }
}
