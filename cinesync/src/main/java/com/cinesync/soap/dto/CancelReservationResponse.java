package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "cancelReservationResponse", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class CancelReservationResponse {
    @XmlElement(required = true)
    private boolean cancelled;

    private String message;

    public CancelReservationResponse() {}

    public CancelReservationResponse(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public CancelReservationResponse(boolean cancelled, String message) {
        this.cancelled = cancelled;
        this.message = message;
    }

    public boolean isCancelled() { return cancelled; }
    public void setCancelled(boolean cancelled) { this.cancelled = cancelled; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
