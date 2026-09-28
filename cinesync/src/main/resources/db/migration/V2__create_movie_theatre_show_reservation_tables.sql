CREATE TABLE movies (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(200) NOT NULL
);

CREATE TABLE theatres (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    rows INTEGER NOT NULL DEFAULT 4 CHECK (rows > 0),
    seats_per_row INTEGER NOT NULL DEFAULT 5 CHECK (seats_per_row > 0)
);

CREATE TABLE shows (
    id BIGSERIAL PRIMARY KEY,
    movie_id BIGINT NOT NULL REFERENCES movies(id),
    theatre_id BIGINT NOT NULL REFERENCES theatres(id),
    showtime TIMESTAMP NOT NULL
);

CREATE TABLE reservations (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    show_id BIGINT NOT NULL REFERENCES shows(id),
    status VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cancelled_at TIMESTAMP NULL
);

CREATE TABLE reservation_seats (
    id BIGSERIAL PRIMARY KEY,
    reservation_id BIGINT NOT NULL REFERENCES reservations(id),
    show_id BIGINT NOT NULL REFERENCES shows(id),
    seat_number VARCHAR(10) NOT NULL,
    released_at TIMESTAMP NULL
);

CREATE UNIQUE INDEX uq_active_show_seat
    ON reservation_seats(show_id, seat_number)
    WHERE released_at IS NULL;

CREATE INDEX idx_reservation_seats_reservation
    ON reservation_seats(reservation_id);
