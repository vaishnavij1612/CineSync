package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "addToWatchlistResponse", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class AddToWatchlistResponse {
    @XmlElement(required = true)
    private boolean added;

    private String message;

    public AddToWatchlistResponse() {}

    public AddToWatchlistResponse(boolean added) {
        this.added = added;
    }

    public AddToWatchlistResponse(boolean added, String message) {
        this.added = added;
        this.message = message;
    }

    public boolean isAdded() { return added; }
    public void setAdded(boolean added) { this.added = added; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
