package com.cinesync.dto;

import com.cinesync.model.ShowtimePreference;
import java.util.List;

public record PreferenceView(List<Long> preferredGenreIds, List<String> preferredGenres,
							 ShowtimePreference showtimePreference) { }
