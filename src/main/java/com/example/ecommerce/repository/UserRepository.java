package com.example.ecommerce.repository;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Page<User> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    Page<User> findByRole(
            Role role,
            Pageable pageable
    );

    Page<User> findByNameContainingIgnoreCaseAndRole(
            String name,
            Role role,
            Pageable pageable
    );
}
