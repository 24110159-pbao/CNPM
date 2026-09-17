package com.example.ecommerce.repository;

import com.example.ecommerce.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    List<OrderItem> findByOrderId(Long orderId);

    boolean existsByOrderIdAndProductId(
            Long orderId,
            Long productId
    );
}
