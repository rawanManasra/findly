package com.findly.domain.entity;

import com.findly.domain.enums.BusinessStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLRestriction;
import org.locationtech.jts.geom.Point;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "businesses", indexes = {
        @Index(name = "idx_businesses_owner", columnList = "owner_id"),
        @Index(name = "idx_businesses_category", columnList = "category_id"),
        @Index(name = "idx_businesses_status", columnList = "status"),
        @Index(name = "idx_businesses_city", columnList = "city")
})
@SQLRestriction("deleted_at IS NULL")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Business extends SoftDeletableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "email", length = 255)
    private String email;

    @Column(name = "website", length = 500)
    private String website;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    // Address fields
    @Column(name = "address_line1", length = 255)
    private String addressLine1;

    @Column(name = "address_line2", length = 255)
    private String addressLine2;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "state", length = 100)
    private String state;

    @Column(name = "postal_code", length = 20)
    private String postalCode;

    @Column(name = "country", length = 100)
    @Builder.Default
    private String country = "Israel";

    // PostGIS location - stored as GEOGRAPHY POINT
    @Column(name = "location", columnDefinition = "geography(Point,4326)")
    private Point location;

    @Column(name = "location_updated_at")
    private Instant locationUpdatedAt;

    // Status fields
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private BusinessStatus status = BusinessStatus.PENDING_APPROVAL;

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private boolean verified = false;

    @Column(name = "rating_avg", precision = 3, scale = 2)
    @Builder.Default
    private BigDecimal ratingAvg = BigDecimal.ZERO;

    @Column(name = "rating_count")
    @Builder.Default
    private Integer ratingCount = 0;

    // Relationships
    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Service> services = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<WorkingHours> workingHours = new ArrayList<>();

    @OneToMany(mappedBy = "business", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Booking> bookings = new ArrayList<>();

    // Helper methods
    public boolean isActive() {
        return status == BusinessStatus.ACTIVE;
    }

    public Double getLatitude() {
        return location != null ? location.getY() : null;
    }

    public Double getLongitude() {
        return location != null ? location.getX() : null;
    }

    public String getFullAddress() {
        StringBuilder sb = new StringBuilder();
        if (addressLine1 != null) sb.append(addressLine1);
        if (addressLine2 != null) sb.append(", ").append(addressLine2);
        if (city != null) sb.append(", ").append(city);
        if (state != null) sb.append(", ").append(state);
        if (postalCode != null) sb.append(" ").append(postalCode);
        if (country != null) sb.append(", ").append(country);
        return sb.toString();
    }

    public void updateLocation(double latitude, double longitude) {
        // Create point using JTS (note: JTS uses X for longitude, Y for latitude)
        org.locationtech.jts.geom.GeometryFactory geometryFactory =
            new org.locationtech.jts.geom.GeometryFactory(
                new org.locationtech.jts.geom.PrecisionModel(), 4326);
        this.location = geometryFactory.createPoint(
            new org.locationtech.jts.geom.Coordinate(longitude, latitude));
        this.locationUpdatedAt = Instant.now();
    }
}
