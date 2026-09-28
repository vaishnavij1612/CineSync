package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "removeFromWatchlistResponse", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class RemoveFromWatchlistResponse {
    @XmlElement(required = true)
    private boolean removed;

    private String message;

    public RemoveFromWatchlistResponse() {}

    public RemoveFromWatchlistResponse(boolean removed) {
        this.removed = removed;
    }

    public RemoveFromWatchlistResponse(boolean removed, String message) {
        this.removed = removed;
        this.message = message;
    }

    public boolean isRemoved() { return removed; }
    public void setRemoved(boolean removed) { this.removed = removed; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
