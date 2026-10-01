package com.example.ecommerce.repository;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByRole(Role role, Pageable pageable);

    Page<User> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    Page<User> findByNameContainingIgnoreCaseAndRole(
            String name,
            Role role,
            Pageable pageable
    );

    // --- Tìm kiếm theo email ---
    Page<User> findByEmailContainingIgnoreCase(
            String email,
            Pageable pageable
    );

    Page<User> findByEmailContainingIgnoreCaseAndRole(
            String email,
            Role role,
            Pageable pageable
    );

    // --- Lọc theo ngày đăng ký ---
    Page<User> findByCreatedAtBetween(
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<User> findByRoleAndCreatedAtBetween(
            Role role,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<User> findByEmailContainingIgnoreCaseAndCreatedAtBetween(
            String email,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    Page<User> findByEmailContainingIgnoreCaseAndRoleAndCreatedAtBetween(
            String email,
            Role role,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );
    Page<User> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
            String name,
            String email,
            Pageable pageable
    );


}
