CREATE TABLE user_preferences (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    showtime_preference VARCHAR(20) NOT NULL DEFAULT 'ANY'
);

CREATE TABLE preference_genres (
    preference_id BIGINT NOT NULL REFERENCES user_preferences(id) ON DELETE CASCADE,
    genre_id BIGINT NOT NULL REFERENCES genres(id) ON DELETE RESTRICT,
    PRIMARY KEY (preference_id, genre_id)
);

CREATE INDEX idx_preference_genres_genre_id ON preference_genres(genre_id);

CREATE TABLE watchlist_items (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    movie_id BIGINT NOT NULL REFERENCES movies(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_watchlist_user_movie UNIQUE (user_id, movie_id)
);

CREATE INDEX idx_watchlist_items_user_id ON watchlist_items(user_id);
CREATE INDEX idx_watchlist_items_movie_id ON watchlist_items(movie_id);
