package com.findly.application.dto.response;

import com.findly.domain.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private UUID id;
    private String email;
    private String phone;
    private String firstName;
    private String lastName;
    private String fullName;
    private UserRole role;
    private boolean emailVerified;
    private boolean phoneVerified;
    private String avatarUrl;
    private Instant createdAt;
}
