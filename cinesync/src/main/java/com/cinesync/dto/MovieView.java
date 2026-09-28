package com.cinesync.dto;

import java.time.LocalDate;
import java.util.List;

public record MovieView(Long id, String title, String overview, Double rating,
                        LocalDate releaseDate, String posterUrl, List<String> genres) { }
