package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "getWatchlistRequest", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetWatchlistRequest {
    @XmlElement(required = true)
    private String username;

    public GetWatchlistRequest() {}

    public GetWatchlistRequest(String username) {
        this.username = username;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
}
