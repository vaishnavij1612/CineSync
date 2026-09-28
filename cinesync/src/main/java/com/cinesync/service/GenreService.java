package com.cinesync.service;

import com.cinesync.model.Genre;
import com.cinesync.repository.GenreRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class GenreService {
    private final GenreRepository genreRepository;

    public GenreService(GenreRepository genreRepository) {
        this.genreRepository = genreRepository;
    }

    @Transactional
    public Genre create(String name) {
        String normalized = name == null ? "" : name.trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Genre name is required");
        }
        if (genreRepository.existsByNameIgnoreCase(normalized)) {
            throw new IllegalArgumentException("Genre already exists");
        }
        try {
            return genreRepository.save(new Genre(normalized));
        } catch (DataIntegrityViolationException exception) {
            throw new IllegalArgumentException("Genre already exists", exception);
        }
    }

    @Transactional(readOnly = true)
    public List<Genre> findAll() {
        return genreRepository.findAll(org.springframework.data.domain.Sort.by("name").ascending());
    }

    @Transactional(readOnly = true)
    public Genre getByName(String name) {
        return genreRepository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new IllegalArgumentException("Genre not found: " + name));
    }
}
