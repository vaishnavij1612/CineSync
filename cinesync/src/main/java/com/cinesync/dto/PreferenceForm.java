package com.cinesync.dto;

import com.cinesync.model.ShowtimePreference;
import java.util.LinkedHashSet;
import java.util.Set;

public class PreferenceForm {
    private Set<Long> genreIds = new LinkedHashSet<>();
    private ShowtimePreference showtimePreference = ShowtimePreference.ANY;

    public Set<Long> getGenreIds() { return genreIds; }
    public void setGenreIds(Set<Long> genreIds) { this.genreIds = genreIds == null ? new LinkedHashSet<>() : genreIds; }
    public ShowtimePreference getShowtimePreference() { return showtimePreference; }
    public void setShowtimePreference(ShowtimePreference showtimePreference) { this.showtimePreference = showtimePreference; }
}
