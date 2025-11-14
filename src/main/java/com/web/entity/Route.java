package com.web.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "routes")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Route {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String code;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String origin;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(name = "distance_km", nullable = false, precision = 8, scale = 2)
    private BigDecimal distanceKm;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // Relaciones
    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stop> stops;

    @OneToMany(mappedBy = "route")
    private List<Trip> trips;

    @OneToMany(mappedBy = "route", cascade = CascadeType.ALL)
    private List<FareRule> fareRules;
}