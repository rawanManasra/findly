package com.findly.domain.entity;

import com.findly.domain.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .role(UserRole.CUSTOMER)
                .build();
    }

    @Nested
    @DisplayName("Full Name Tests")
    class FullNameTests {

        @Test
        @DisplayName("Should return full name with first and last name")
        void shouldReturnFullName() {
            assertThat(user.getFullName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("Should return only first name when last name is null")
        void shouldReturnFirstNameWhenLastNameNull() {
            user.setLastName(null);
            assertThat(user.getFullName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should return only first name when last name is blank")
        void shouldReturnFirstNameWhenLastNameBlank() {
            user.setLastName("   ");
            assertThat(user.getFullName()).isEqualTo("John");
        }

        @Test
        @DisplayName("Should return only first name when last name is empty")
        void shouldReturnFirstNameWhenLastNameEmpty() {
            user.setLastName("");
            assertThat(user.getFullName()).isEqualTo("John");
        }
    }

    @Nested
    @DisplayName("Role Check Tests")
    class RoleCheckTests {

        @Test
        @DisplayName("Should return true for isBusinessOwner when role is BUSINESS_OWNER")
        void shouldReturnTrueForBusinessOwner() {
            user.setRole(UserRole.BUSINESS_OWNER);
            assertThat(user.isBusinessOwner()).isTrue();
        }

        @Test
        @DisplayName("Should return false for isBusinessOwner when role is CUSTOMER")
        void shouldReturnFalseForBusinessOwnerWhenCustomer() {
            user.setRole(UserRole.CUSTOMER);
            assertThat(user.isBusinessOwner()).isFalse();
        }

        @Test
        @DisplayName("Should return true for isAdmin when role is ADMIN")
        void shouldReturnTrueForAdmin() {
            user.setRole(UserRole.ADMIN);
            assertThat(user.isAdmin()).isTrue();
        }

        @Test
        @DisplayName("Should return false for isAdmin when role is CUSTOMER")
        void shouldReturnFalseForAdminWhenCustomer() {
            user.setRole(UserRole.CUSTOMER);
            assertThat(user.isAdmin()).isFalse();
        }

        @Test
        @DisplayName("Should return false for isAdmin when role is BUSINESS_OWNER")
        void shouldReturnFalseForAdminWhenBusinessOwner() {
            user.setRole(UserRole.BUSINESS_OWNER);
            assertThat(user.isAdmin()).isFalse();
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should have default role of CUSTOMER")
        void shouldHaveDefaultCustomerRole() {
            User newUser = User.builder()
                    .email("new@example.com")
                    .firstName("New")
                    .build();

            assertThat(newUser.getRole()).isEqualTo(UserRole.CUSTOMER);
        }

        @Test
        @DisplayName("Should have emailVerified as false by default")
        void shouldNotHaveEmailVerifiedByDefault() {
            User newUser = User.builder()
                    .email("new@example.com")
                    .firstName("New")
                    .build();

            assertThat(newUser.isEmailVerified()).isFalse();
        }

        @Test
        @DisplayName("Should have phoneVerified as false by default")
        void shouldNotHavePhoneVerifiedByDefault() {
            User newUser = User.builder()
                    .email("new@example.com")
                    .firstName("New")
                    .build();

            assertThat(newUser.isPhoneVerified()).isFalse();
        }

        @Test
        @DisplayName("Should have empty lists by default")
        void shouldHaveEmptyListsByDefault() {
            User newUser = User.builder()
                    .email("new@example.com")
                    .firstName("New")
                    .build();

            assertThat(newUser.getBusinesses()).isEmpty();
            assertThat(newUser.getBookings()).isEmpty();
        }
    }
}
