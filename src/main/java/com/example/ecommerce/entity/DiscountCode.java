package com.example.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "discount_codes",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_discount_codes_code",
                        columnNames = "code"
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiscountCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * Phần trăm giảm giá.
     * Ví dụ: 20 = giảm 20%.
     */
    @Column(nullable = false)
    private Integer discountValue;

    /**
     * Số lượt sử dụng còn lại.
     */
    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private LocalDateTime startAt;

    @Column(nullable = false)
    private LocalDateTime endAt;
}
