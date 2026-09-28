package com.cinesync.soap.dto;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.ArrayList;
import java.util.List;

@XmlRootElement(name = "getWatchlistResponse", namespace = "http://cinesync.com/soap")
@XmlAccessorType(XmlAccessType.FIELD)
public class GetWatchlistResponse {
    @XmlElement(name = "items")
    private List<WatchlistItemSoapDto> items = new ArrayList<>();

    public GetWatchlistResponse() {}

    public GetWatchlistResponse(List<WatchlistItemSoapDto> items) {
        this.items = items != null ? items : new ArrayList<>();
    }

    public List<WatchlistItemSoapDto> getItems() { return items; }
    public void setItems(List<WatchlistItemSoapDto> items) { this.items = items; }
}
