package com.findly.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

@Entity
@Table(name = "working_hours",
        uniqueConstraints = @UniqueConstraint(columnNames = {"business_id", "day_of_week"}),
        indexes = @Index(name = "idx_working_hours_business", columnList = "business_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WorkingHours {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "business_id", nullable = false)
    private Business business;

    @Column(name = "day_of_week", nullable = false, columnDefinition = "SMALLINT")
    private Integer dayOfWeek; // 0 = Sunday, 6 = Saturday

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "is_closed", nullable = false)
    @Builder.Default
    private boolean closed = false;

    @Column(name = "break_start")
    private LocalTime breakStart;

    @Column(name = "break_end")
    private LocalTime breakEnd;

    public boolean hasBreak() {
        return breakStart != null && breakEnd != null;
    }

    public boolean isOpen() {
        return !closed && startTime != null && endTime != null;
    }

    public String getDayName() {
        return switch (dayOfWeek) {
            case 0 -> "Sunday";
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            case 6 -> "Saturday";
            default -> "Unknown";
        };
    }

    public DayOfWeek toDayOfWeek() {
        // Java DayOfWeek: Monday = 1, Sunday = 7
        // Our format: Sunday = 0, Saturday = 6
        return dayOfWeek == 0 ? DayOfWeek.SUNDAY : DayOfWeek.of(dayOfWeek);
    }

    public static int fromDayOfWeek(DayOfWeek day) {
        return day == DayOfWeek.SUNDAY ? 0 : day.getValue();
    }
}
