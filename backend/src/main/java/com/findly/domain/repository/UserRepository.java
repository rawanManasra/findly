package com.findly.domain.repository;

import com.findly.domain.entity.User;
import com.findly.domain.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPhone(String phone);

    boolean existsByEmail(String email);

    boolean existsByPhone(String phone);

    @Query("SELECT u FROM User u WHERE u.email = :email OR u.phone = :phone")
    Optional<User> findByEmailOrPhone(@Param("email") String email, @Param("phone") String phone);

    long countByRole(UserRole role);
}
