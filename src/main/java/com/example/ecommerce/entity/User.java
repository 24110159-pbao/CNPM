package com.example.ecommerce.entity;

import com.example.ecommerce.enums.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "users",
        indexes = {
                @Index(name = "idx_users_email", columnList = "email")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(length = 20)
    private String phone;

    @Column(length = 255)
    private String address;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.USER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    /*
     * User 1 - 1 Cart
     */
    @OneToOne(
            mappedBy = "user"
    )
    private Cart cart;

    /*
     * User 1 - N Orders
     */
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    /*
     * User 1 - N Reviews
     */
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Review> reviews = new ArrayList<>();

    /*
     * User 1 - N Notifications
     */
    @OneToMany(
            mappedBy = "user",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Notification> notifications = new ArrayList<>();
}
