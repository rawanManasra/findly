package com.findly.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class WorkingHoursTest {

    private WorkingHours workingHours;

    @BeforeEach
    void setUp() {
        workingHours = WorkingHours.builder()
                .dayOfWeek(1) // Monday
                .startTime(LocalTime.of(9, 0))
                .endTime(LocalTime.of(18, 0))
                .closed(false)
                .build();
    }

    @Nested
    @DisplayName("hasBreak Tests")
    class HasBreakTests {

        @Test
        @DisplayName("Should return true when both break times are set")
        void shouldReturnTrueWhenBreakTimesSet() {
            workingHours.setBreakStart(LocalTime.of(12, 0));
            workingHours.setBreakEnd(LocalTime.of(13, 0));

            assertThat(workingHours.hasBreak()).isTrue();
        }

        @Test
        @DisplayName("Should return false when break start is null")
        void shouldReturnFalseWhenBreakStartNull() {
            workingHours.setBreakStart(null);
            workingHours.setBreakEnd(LocalTime.of(13, 0));

            assertThat(workingHours.hasBreak()).isFalse();
        }

        @Test
        @DisplayName("Should return false when break end is null")
        void shouldReturnFalseWhenBreakEndNull() {
            workingHours.setBreakStart(LocalTime.of(12, 0));
            workingHours.setBreakEnd(null);

            assertThat(workingHours.hasBreak()).isFalse();
        }

        @Test
        @DisplayName("Should return false when both break times are null")
        void shouldReturnFalseWhenBothBreakTimesNull() {
            workingHours.setBreakStart(null);
            workingHours.setBreakEnd(null);

            assertThat(workingHours.hasBreak()).isFalse();
        }
    }

    @Nested
    @DisplayName("isOpen Tests")
    class IsOpenTests {

        @Test
        @DisplayName("Should return true when not closed and times set")
        void shouldReturnTrueWhenOpen() {
            workingHours.setClosed(false);
            workingHours.setStartTime(LocalTime.of(9, 0));
            workingHours.setEndTime(LocalTime.of(18, 0));

            assertThat(workingHours.isOpen()).isTrue();
        }

        @Test
        @DisplayName("Should return false when closed")
        void shouldReturnFalseWhenClosed() {
            workingHours.setClosed(true);

            assertThat(workingHours.isOpen()).isFalse();
        }

        @Test
        @DisplayName("Should return false when start time is null")
        void shouldReturnFalseWhenStartTimeNull() {
            workingHours.setClosed(false);
            workingHours.setStartTime(null);
            workingHours.setEndTime(LocalTime.of(18, 0));

            assertThat(workingHours.isOpen()).isFalse();
        }

        @Test
        @DisplayName("Should return false when end time is null")
        void shouldReturnFalseWhenEndTimeNull() {
            workingHours.setClosed(false);
            workingHours.setStartTime(LocalTime.of(9, 0));
            workingHours.setEndTime(null);

            assertThat(workingHours.isOpen()).isFalse();
        }
    }

    @Nested
    @DisplayName("getDayName Tests")
    class GetDayNameTests {

        @Test
        @DisplayName("Should return Sunday for day 0")
        void shouldReturnSunday() {
            workingHours.setDayOfWeek(0);
            assertThat(workingHours.getDayName()).isEqualTo("Sunday");
        }

        @Test
        @DisplayName("Should return Monday for day 1")
        void shouldReturnMonday() {
            workingHours.setDayOfWeek(1);
            assertThat(workingHours.getDayName()).isEqualTo("Monday");
        }

        @Test
        @DisplayName("Should return Tuesday for day 2")
        void shouldReturnTuesday() {
            workingHours.setDayOfWeek(2);
            assertThat(workingHours.getDayName()).isEqualTo("Tuesday");
        }

        @Test
        @DisplayName("Should return Wednesday for day 3")
        void shouldReturnWednesday() {
            workingHours.setDayOfWeek(3);
            assertThat(workingHours.getDayName()).isEqualTo("Wednesday");
        }

        @Test
        @DisplayName("Should return Thursday for day 4")
        void shouldReturnThursday() {
            workingHours.setDayOfWeek(4);
            assertThat(workingHours.getDayName()).isEqualTo("Thursday");
        }

        @Test
        @DisplayName("Should return Friday for day 5")
        void shouldReturnFriday() {
            workingHours.setDayOfWeek(5);
            assertThat(workingHours.getDayName()).isEqualTo("Friday");
        }

        @Test
        @DisplayName("Should return Saturday for day 6")
        void shouldReturnSaturday() {
            workingHours.setDayOfWeek(6);
            assertThat(workingHours.getDayName()).isEqualTo("Saturday");
        }

        @Test
        @DisplayName("Should return Unknown for invalid day")
        void shouldReturnUnknownForInvalidDay() {
            workingHours.setDayOfWeek(7);
            assertThat(workingHours.getDayName()).isEqualTo("Unknown");
        }
    }

    @Nested
    @DisplayName("toDayOfWeek Tests")
    class ToDayOfWeekTests {

        @Test
        @DisplayName("Should convert Sunday (0) to DayOfWeek.SUNDAY")
        void shouldConvertSunday() {
            workingHours.setDayOfWeek(0);
            assertThat(workingHours.toDayOfWeek()).isEqualTo(DayOfWeek.SUNDAY);
        }

        @Test
        @DisplayName("Should convert Monday (1) to DayOfWeek.MONDAY")
        void shouldConvertMonday() {
            workingHours.setDayOfWeek(1);
            assertThat(workingHours.toDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        }

        @Test
        @DisplayName("Should convert Friday (5) to DayOfWeek.FRIDAY")
        void shouldConvertFriday() {
            workingHours.setDayOfWeek(5);
            assertThat(workingHours.toDayOfWeek()).isEqualTo(DayOfWeek.FRIDAY);
        }

        @Test
        @DisplayName("Should convert Saturday (6) to DayOfWeek.SATURDAY")
        void shouldConvertSaturday() {
            workingHours.setDayOfWeek(6);
            assertThat(workingHours.toDayOfWeek()).isEqualTo(DayOfWeek.SATURDAY);
        }
    }

    @Nested
    @DisplayName("fromDayOfWeek Tests")
    class FromDayOfWeekTests {

        @Test
        @DisplayName("Should convert DayOfWeek.SUNDAY to 0")
        void shouldConvertFromSunday() {
            assertThat(WorkingHours.fromDayOfWeek(DayOfWeek.SUNDAY)).isEqualTo(0);
        }

        @Test
        @DisplayName("Should convert DayOfWeek.MONDAY to 1")
        void shouldConvertFromMonday() {
            assertThat(WorkingHours.fromDayOfWeek(DayOfWeek.MONDAY)).isEqualTo(1);
        }

        @Test
        @DisplayName("Should convert DayOfWeek.FRIDAY to 5")
        void shouldConvertFromFriday() {
            assertThat(WorkingHours.fromDayOfWeek(DayOfWeek.FRIDAY)).isEqualTo(5);
        }

        @Test
        @DisplayName("Should convert DayOfWeek.SATURDAY to 6")
        void shouldConvertFromSaturday() {
            assertThat(WorkingHours.fromDayOfWeek(DayOfWeek.SATURDAY)).isEqualTo(6);
        }
    }
}
