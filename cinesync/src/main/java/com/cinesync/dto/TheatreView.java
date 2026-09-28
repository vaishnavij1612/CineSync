package com.cinesync.dto;

public record TheatreView(Long id, String name, String address, Double latitude,
                          Double longitude, String status) { }
