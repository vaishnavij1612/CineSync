package com.cinesync.service;

import com.cinesync.dto.TheatreView;
import com.cinesync.model.Theatre;
import com.cinesync.repository.TheatreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TheatreService {
    private final TheatreRepository theatreRepository;

    public TheatreService(TheatreRepository theatreRepository) {
        this.theatreRepository = theatreRepository;
    }

    @Transactional(readOnly = true)
    public List<TheatreView> findAll() {
        return theatreRepository.findAll(org.springframework.data.domain.Sort.by("name").ascending())
                .stream().map(this::toView).toList();
    }

    @Transactional
    public Theatre save(Theatre theatre) {
        if (theatre == null || theatre.getName() == null || theatre.getName().isBlank()) {
            throw new IllegalArgumentException("Theatre name is required");
        }
        if (theatre.getRows() <= 0 || theatre.getSeatsPerRow() <= 0) {
            throw new IllegalArgumentException("Theatre layout must contain seats");
        }
        return theatreRepository.save(theatre);
    }

    public TheatreView toView(Theatre theatre) {
        return new TheatreView(theatre.getId(), theatre.getName(), theatre.getAddress(),
                theatre.getLatitude(), theatre.getLongitude(), theatre.getStatus());
    }
}
