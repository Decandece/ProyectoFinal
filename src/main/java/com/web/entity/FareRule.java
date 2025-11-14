package com.web.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.Map;

@Entity
@Table(name = "fare_rules")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FareRule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "route_id", nullable = false)
    private Route route;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "from_stop_id", nullable = false)
    private Stop fromStop;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "to_stop_id", nullable = false)
    private Stop toStop;

    @Column(name = "base_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal basePrice;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> discounts;

    @Column(name = "dynamic_pricing_enabled", nullable = false)
    @Builder.Default
    private Boolean dynamicPricingEnabled = false;
}

