CREATE TABLE genres (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE
);

CREATE TABLE movie_genres (
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genres(id) ON DELETE RESTRICT,
    PRIMARY KEY (movie_id, genre_id)
);

CREATE INDEX idx_movie_genres_genre_id ON movie_genres(genre_id);

ALTER TABLE movies
    ADD COLUMN overview TEXT,
    ADD COLUMN rating DOUBLE PRECISION,
    ADD COLUMN release_date DATE,
    ADD COLUMN poster_url VARCHAR(500),
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE theatres
    ADD COLUMN address VARCHAR(300),
    ADD COLUMN latitude DOUBLE PRECISION,
    ADD COLUMN longitude DOUBLE PRECISION,
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ADD COLUMN updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;

ALTER TABLE shows
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    ADD COLUMN seat_capacity INTEGER NOT NULL DEFAULT 20 CHECK (seat_capacity > 0);

CREATE INDEX idx_shows_showtime ON shows(showtime);
CREATE INDEX idx_shows_movie_id ON shows(movie_id);
CREATE INDEX idx_shows_theatre_id ON shows(theatre_id);
