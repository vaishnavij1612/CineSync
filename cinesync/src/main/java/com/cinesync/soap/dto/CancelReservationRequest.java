package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "cancelReservationRequest", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class CancelReservationRequest {
    @XmlElement(required = true)
    private String username;

    @XmlElement(required = true)
    private Long reservationId;

    public CancelReservationRequest() {}

    public CancelReservationRequest(String username, Long reservationId) {
        this.username = username;
        this.reservationId = reservationId;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Long getReservationId() { return reservationId; }
    public void setReservationId(Long reservationId) { this.reservationId = reservationId; }
}
