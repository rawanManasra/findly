package com.findly.domain.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTest {

    private Service service;

    @BeforeEach
    void setUp() {
        service = Service.builder()
                .name("Test Service")
                .durationMins(60)
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("Formatted Price Tests")
    class FormattedPriceTests {

        @Test
        @DisplayName("Should return 'Price on request' when price is null")
        void shouldReturnPriceOnRequestWhenNull() {
            service.setPrice(null);
            assertThat(service.getFormattedPrice()).isEqualTo("Price on request");
        }

        @Test
        @DisplayName("Should format price with currency")
        void shouldFormatPriceWithCurrency() {
            service.setPrice(new BigDecimal("150.00"));
            service.setCurrency("ILS");
            assertThat(service.getFormattedPrice()).isEqualTo("ILS 150.00");
        }

        @Test
        @DisplayName("Should format price with USD currency")
        void shouldFormatPriceWithUsdCurrency() {
            service.setPrice(new BigDecimal("50.00"));
            service.setCurrency("USD");
            assertThat(service.getFormattedPrice()).isEqualTo("USD 50.00");
        }
    }

    @Nested
    @DisplayName("Formatted Duration Tests")
    class FormattedDurationTests {

        @Test
        @DisplayName("Should format duration under 60 mins as minutes")
        void shouldFormatDurationUnder60Mins() {
            service.setDurationMins(30);
            assertThat(service.getFormattedDuration()).isEqualTo("30 mins");
        }

        @Test
        @DisplayName("Should format duration of exactly 1 hour")
        void shouldFormatDurationOneHour() {
            service.setDurationMins(60);
            assertThat(service.getFormattedDuration()).isEqualTo("1 hour");
        }

        @Test
        @DisplayName("Should format duration of exactly 2 hours")
        void shouldFormatDurationTwoHours() {
            service.setDurationMins(120);
            assertThat(service.getFormattedDuration()).isEqualTo("2 hours");
        }

        @Test
        @DisplayName("Should format duration with hours and minutes")
        void shouldFormatDurationHoursAndMinutes() {
            service.setDurationMins(90);
            assertThat(service.getFormattedDuration()).isEqualTo("1h 30m");
        }

        @Test
        @DisplayName("Should format 2h30m duration")
        void shouldFormat2h30mDuration() {
            service.setDurationMins(150);
            assertThat(service.getFormattedDuration()).isEqualTo("2h 30m");
        }

        @Test
        @DisplayName("Should format 45 minutes")
        void shouldFormat45Minutes() {
            service.setDurationMins(45);
            assertThat(service.getFormattedDuration()).isEqualTo("45 mins");
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should have default duration of 30 minutes")
        void shouldHaveDefaultDuration() {
            Service newService = Service.builder()
                    .name("New Service")
                    .build();

            assertThat(newService.getDurationMins()).isEqualTo(30);
        }

        @Test
        @DisplayName("Should have default currency of ILS")
        void shouldHaveDefaultCurrency() {
            Service newService = Service.builder()
                    .name("New Service")
                    .build();

            assertThat(newService.getCurrency()).isEqualTo("ILS");
        }

        @Test
        @DisplayName("Should be active by default")
        void shouldBeActiveByDefault() {
            Service newService = Service.builder()
                    .name("New Service")
                    .build();

            assertThat(newService.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should have sort order 0 by default")
        void shouldHaveSortOrderZeroByDefault() {
            Service newService = Service.builder()
                    .name("New Service")
                    .build();

            assertThat(newService.getSortOrder()).isEqualTo(0);
        }

        @Test
        @DisplayName("Should have empty bookings list by default")
        void shouldHaveEmptyBookingsByDefault() {
            Service newService = Service.builder()
                    .name("New Service")
                    .build();

            assertThat(newService.getBookings()).isEmpty();
        }
    }
}
