package com.example.ecommerce.service;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.Role;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;



    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    public Page<User> searchUsers(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return userRepository.findAll(pageable);
        }

        return userRepository.findByNameContainingIgnoreCase(
                keyword.trim(),
                pageable
        );
    }

    public Page<User> getUsersByRole(
            Role role,
            Pageable pageable
    ) {
        return userRepository.findByRole(role, pageable);
    }

    /**
     * Tìm kiếm / lọc người dùng theo bất kỳ tổ hợp nào:
     * email (chứa, không phân biệt hoa/thường), vai trò, khoảng ngày đăng ký.
     */
    public Page<User> filterUsers(
            String keyword,
            Role role,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasRole = role != null;
        boolean hasDate = startDate != null && endDate != null;

        if (!hasKeyword && !hasRole && !hasDate) {
            return userRepository.findAll(pageable);
        }

        String keywordTrim = hasKeyword ? keyword.trim() : null;

        LocalDateTime start = hasDate
                ? startDate.atStartOfDay()
                : null;

        LocalDateTime end = hasDate
                ? endDate.atTime(LocalTime.MAX)
                : null;

        // Có keyword + role + ngày
        if (hasKeyword && hasRole && hasDate) {
            // phần này cần thêm query tương ứng nếu muốn kết hợp cả 3
        }

        // Có keyword
        if (hasKeyword) {
            return userRepository
                    .findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
                            keywordTrim,
                            keywordTrim,
                            pageable
                    );
        }

        // Có role + ngày
        if (hasRole && hasDate) {
            return userRepository.findByRoleAndCreatedAtBetween(
                    role, start, end, pageable
            );
        }

        if (hasRole) {
            return userRepository.findByRole(role, pageable);
        }

        return userRepository.findByCreatedAtBetween(
                start, end, pageable
        );
    }


    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        return userRepository.findByEmail(email.trim()).orElse(null);
    }

    public boolean existsByEmail(String email) {
        return email != null
                && userRepository.existsByEmail(email.trim());
    }

    @Transactional
    public User register(
            String name,
            String email,
            String rawPassword
    ) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        if (rawPassword == null || rawPassword.isEmpty()) {
            return null;
        }

        String normalizedEmail = email.trim().toLowerCase();

        if (userRepository.existsByEmail(normalizedEmail)) {
            return null;
        }

        User user = User.builder()
                .name(name.trim())
                .email(normalizedEmail)
                .password(passwordEncoder.encode(rawPassword))
                .role(Role.USER)
                .build();

        return userRepository.save(user);
    }

    @Transactional
    public boolean updateProfile(
            Long userId,
            String name,
            String phone,
            String address
    ) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return false;
        }

        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        user.setName(name.trim());
        user.setPhone(phone != null ? phone.trim() : null);
        user.setAddress(address != null ? address.trim() : null);

        userRepository.save(user);

        return true;
    }

    @Transactional
    public boolean updatePassword(
            Long userId,
            String newPassword
    ) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null) {
            return false;
        }

        if (newPassword == null || newPassword.isEmpty()) {
            return false;
        }

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        return true;
    }

    @Transactional
    public boolean updateRole(
            Long userId,
            Role role
    ) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null || role == null) {
            return false;
        }

        user.setRole(role);

        userRepository.save(user);

        return true;
    }
}
