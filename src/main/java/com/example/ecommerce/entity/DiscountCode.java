package com.example.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "discount_codes",
        indexes = {
                @Index(name = "idx_discount_code", columnList = "code")
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

    /*
     * Ví dụ:
     * 20 = 20%
     */
    @Column(
            name = "discount_value",
            nullable = false,
            precision = 5,
            scale = 2
    )
    private BigDecimal discountValue;

    /*
     * Số lượt còn lại
     */
    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "start_at", nullable = false)
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false)
    private LocalDateTime endAt;

    /*
     * Manager có thể vô hiệu hóa code
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @OneToMany(
            mappedBy = "discountCode",
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<Order> orders = new ArrayList<>();
}
