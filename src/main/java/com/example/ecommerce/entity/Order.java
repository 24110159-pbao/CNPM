package com.example.ecommerce.entity;

import com.example.ecommerce.enums.OrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(name = "idx_orders_user", columnList = "user_id"),
                @Index(name = "idx_orders_status", columnList = "status"),
                @Index(name = "idx_orders_created_at", columnList = "created_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /*
     * User đặt hàng
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "user_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_user")
    )
    private User user;

    /*
     * Snapshot thông tin người nhận
     */
    @Column(name = "recipient_name", nullable = false, length = 100)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false, length = 20)
    private String recipientPhone;

    @Column(name = "shipping_address", nullable = false, length = 500)
    private String shippingAddress;

    /*
     * Tổng tiền hàng trước giảm giá
     */
    @Column(name = "subtotal", nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    /*
     * Số tiền được giảm
     */
    @Column(name = "discount_amount", nullable = false, precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    /*
     * Tổng tiền cuối cùng
     */
    @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal totalAmount;

    /*
     * Trạng thái Order
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private OrderStatus status = OrderStatus.PENDING;

    /*
     * Discount code đã sử dụng.
     * Có thể null nếu không dùng mã.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "discount_code_id",
            foreignKey = @ForeignKey(name = "fk_order_discount")
    )
    private DiscountCode discountCode;

    @Column(name = "created_at", nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /*
     * Order 1 - N OrderItem
     */
    @OneToMany(
            mappedBy = "order",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<OrderItem> items = new ArrayList<>();

    /*
     * Order 1 - 1 Payment
     */
    @OneToOne(
            mappedBy = "order",
            fetch = FetchType.LAZY
    )
    private Payment payment;
}
