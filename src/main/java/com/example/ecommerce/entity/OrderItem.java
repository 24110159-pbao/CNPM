package com.example.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "order_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_order")
    )
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "product_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_order_items_product")
    )
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    /**
     * Giá sản phẩm tại thời điểm đặt hàng.
     * Không lấy lại giá từ Product khi xem lịch sử Order.
     */
    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;
}
