package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "createReservationRequest", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class CreateReservationRequest {
    @XmlElement(required = true)
    private String username;

    @XmlElement(required = true)
    private Long showId;

    @XmlElement(name = "seats", required = true)
    private List<String> seats = new ArrayList<>();

    public CreateReservationRequest() {}

    public CreateReservationRequest(String username, Long showId, List<String> seats) {
        this.username = username;
        this.showId = showId;
        this.seats = seats != null ? seats : new ArrayList<>();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getShowId() { return showId; }
    public void setShowId(Long showId) { this.showId = showId; }
    public List<String> getSeats() { return seats; }
    public void setSeats(List<String> seats) { this.seats = seats; }
}
