package com.example.ecommerce.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http
    ) throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        // Guest được phép truy cập
                        .requestMatchers(
                                "/",
                                "/products",
                                "/products/**",
                                "/categories",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // Các request khác tạm thời cho phép
                        // để phát triển phần Guest.
                        .anyRequest().permitAll()
                )

                // Tắt login mặc định của Spring Security
                .formLogin(form -> form.disable())

                // Tạm thời tắt logout mặc định
                .logout(logout -> logout.disable());

        return http.build();
    }
}
