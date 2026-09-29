package com.example.ecommerce.security;

import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        if (email == null || email.trim().isEmpty()) {
            throw new UsernameNotFoundException(
                    "Email không hợp lệ"
            );
        }

        User user = userRepository
                .findByEmail(email.trim().toLowerCase())
                .orElse(null);

        if (user == null) {
            throw new UsernameNotFoundException(
                    "Không tìm thấy người dùng"
            );
        }

        return new CustomUserDetails(user);
    }
}
