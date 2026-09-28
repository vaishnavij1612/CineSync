package com.cinesync.dto;

import java.time.LocalDateTime;

public record WatchlistView(MovieView movie, LocalDateTime addedAt) { }
