package com.web.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "stops")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Stop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "stop_order", nullable = false)
    private Integer order;

    @Column(precision = 10, scale = 8)
    private BigDecimal latitude;

    @Column(precision = 11, scale = 8)
    private BigDecimal longitude;

    // Relaciones inversas
    @OneToMany(mappedBy = "fromStop")
    private List<Ticket> ticketsFrom;

    @OneToMany(mappedBy = "toStop")
    private List<Ticket> ticketsTo;

    @OneToMany(mappedBy = "fromStop")
    private List<FareRule> fareRulesFrom;

    @OneToMany(mappedBy = "toStop")
    private List<FareRule> fareRulesTo;

    @OneToMany(mappedBy = "fromStop")
    private List<Parcel> parcelsFrom;

    @OneToMany(mappedBy = "toStop")
    private List<Parcel> parcelsTo;
}
