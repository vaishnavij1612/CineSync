package com.cinesync.model;

import jakarta.persistence.*;

@Entity
@Table(name = "genres")
public class Genre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    protected Genre() { }

    public Genre(String name) { this.name = name; }

    public Long getId() { return id; }
    public String getName() { return name; }
}
