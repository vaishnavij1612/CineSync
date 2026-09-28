package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "getReservationsResponse", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetReservationsResponse {
    @XmlElement(name = "reservations")
    private List<ReservationSoapDto> reservations = new ArrayList<>();

    public GetReservationsResponse() {}

    public GetReservationsResponse(List<ReservationSoapDto> reservations) {
        this.reservations = reservations != null ? reservations : new ArrayList<>();
    }

    public List<ReservationSoapDto> getReservations() { return reservations; }
    public void setReservations(List<ReservationSoapDto> reservations) { this.reservations = reservations; }
}
