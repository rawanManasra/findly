package com.findly.domain.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "services", indexes = {
        @Index(name = "idx_services_business", columnList = "business_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Service extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "duration_mins", nullable = false)
    @Builder.Default
    private Integer durationMins = 30;

    @Column(name = "price", precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "currency", length = 3)
    @Builder.Default
    private String currency = "ILS";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    @Builder.Default
    private Integer sortOrder = 0;

    @OneToMany(mappedBy = "service", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    public String getFormattedPrice() {
        if (price == null) {
            return "Price on request";
        }
        return currency + " " + price.toString();
    }

    public String getFormattedDuration() {
        if (durationMins < 60) {
            return durationMins + " mins";
        }
        int hours = durationMins / 60;
        int mins = durationMins % 60;
        if (mins == 0) {
            return hours + " hour" + (hours > 1 ? "s" : "");
        }
        return hours + "h " + mins + "m";
    }
}
