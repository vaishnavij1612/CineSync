package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "updateUserPreferencesResponse", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class UpdateUserPreferencesResponse {
    @XmlElement(required = true)
    private boolean updated;

    private String message;

    public UpdateUserPreferencesResponse() {}

    public UpdateUserPreferencesResponse(boolean updated) {
        this.updated = updated;
    }

    public UpdateUserPreferencesResponse(boolean updated, String message) {
        this.updated = updated;
        this.message = message;
    }

    public boolean isUpdated() { return updated; }
    public void setUpdated(boolean updated) { this.updated = updated; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
