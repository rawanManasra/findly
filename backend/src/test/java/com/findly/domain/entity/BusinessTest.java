package com.findly.domain.entity;

import com.findly.domain.enums.BusinessStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class BusinessTest {

    private Business business;

    @BeforeEach
    void setUp() {
        business = Business.builder()
                .name("Test Business")
                .description("A test business")
                .status(BusinessStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("isActive Tests")
    class IsActiveTests {

        @Test
        @DisplayName("Should return true when status is ACTIVE")
        void shouldReturnTrueWhenActive() {
            business.setStatus(BusinessStatus.ACTIVE);
            assertThat(business.isActive()).isTrue();
        }

        @Test
        @DisplayName("Should return false when status is INACTIVE")
        void shouldReturnFalseWhenInactive() {
            business.setStatus(BusinessStatus.INACTIVE);
            assertThat(business.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should return false when status is PENDING_APPROVAL")
        void shouldReturnFalseWhenPending() {
            business.setStatus(BusinessStatus.PENDING_APPROVAL);
            assertThat(business.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should return false when status is SUSPENDED")
        void shouldReturnFalseWhenSuspended() {
            business.setStatus(BusinessStatus.SUSPENDED);
            assertThat(business.isActive()).isFalse();
        }
    }

    @Nested
    @DisplayName("Location Tests")
    class LocationTests {

        @Test
        @DisplayName("Should return null latitude when location is null")
        void shouldReturnNullLatitudeWhenLocationNull() {
            business.setLocation(null);
            assertThat(business.getLatitude()).isNull();
        }

        @Test
        @DisplayName("Should return null longitude when location is null")
        void shouldReturnNullLongitudeWhenLocationNull() {
            business.setLocation(null);
            assertThat(business.getLongitude()).isNull();
        }

        @Test
        @DisplayName("Should update location correctly")
        void shouldUpdateLocation() {
            double lat = 32.0853;
            double lng = 34.7818;

            business.updateLocation(lat, lng);

            assertThat(business.getLocation()).isNotNull();
            assertThat(business.getLatitude()).isCloseTo(lat, org.assertj.core.data.Offset.offset(0.0001));
            assertThat(business.getLongitude()).isCloseTo(lng, org.assertj.core.data.Offset.offset(0.0001));
            assertThat(business.getLocationUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Full Address Tests")
    class FullAddressTests {

        @Test
        @DisplayName("Should build full address with all fields")
        void shouldBuildFullAddress() {
            business.setAddressLine1("123 Main St");
            business.setAddressLine2("Suite 100");
            business.setCity("Tel Aviv");
            business.setState("Tel Aviv District");
            business.setPostalCode("12345");
            business.setCountry("Israel");

            String fullAddress = business.getFullAddress();

            assertThat(fullAddress).contains("123 Main St");
            assertThat(fullAddress).contains("Suite 100");
            assertThat(fullAddress).contains("Tel Aviv");
            assertThat(fullAddress).contains("12345");
            assertThat(fullAddress).contains("Israel");
        }

        @Test
        @DisplayName("Should handle missing address fields")
        void shouldHandleMissingAddressFields() {
            business.setCity("Tel Aviv");
            business.setCountry("Israel");

            String fullAddress = business.getFullAddress();

            assertThat(fullAddress).contains("Tel Aviv");
            assertThat(fullAddress).contains("Israel");
            assertThat(fullAddress).doesNotContain("null");
        }

        @Test
        @DisplayName("Should return empty string when no address fields set")
        void shouldReturnEmptyWhenNoAddressFields() {
            business.setAddressLine1(null);
            business.setCity(null);
            business.setCountry(null);

            String fullAddress = business.getFullAddress();

            assertThat(fullAddress).isEmpty();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should have default status of PENDING_APPROVAL")
        void shouldHaveDefaultPendingStatus() {
            Business newBusiness = Business.builder()
                    .name("New Business")
                    .build();

            assertThat(newBusiness.getStatus()).isEqualTo(BusinessStatus.PENDING_APPROVAL);
        }

        @Test
        @DisplayName("Should have default country of Israel")
        void shouldHaveDefaultCountryIsrael() {
            Business newBusiness = Business.builder()
                    .name("New Business")
                    .build();

            assertThat(newBusiness.getCountry()).isEqualTo("Israel");
        }

        @Test
        @DisplayName("Should have verified as false by default")
        void shouldNotBeVerifiedByDefault() {
            Business newBusiness = Business.builder()
                    .name("New Business")
                    .build();

            assertThat(newBusiness.isVerified()).isFalse();
        }

        @Test
        @DisplayName("Should have empty lists by default")
        void shouldHaveEmptyListsByDefault() {
            Business newBusiness = Business.builder()
                    .name("New Business")
                    .build();

            assertThat(newBusiness.getServices()).isEmpty();
            assertThat(newBusiness.getWorkingHours()).isEmpty();
            assertThat(newBusiness.getBookings()).isEmpty();
        }
    }
}
