package com.example.ecommerce.service;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;

    public Page<Order> getUserOrders(
            Long userId,
            Pageable pageable
    ) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(
                userId,
                pageable
        );
    }

    public Page<Order> getUserOrdersByStatus(
            Long userId,
            OrderStatus status,
            Pageable pageable
    ) {
        return orderRepository.findByUserIdAndStatusOrderByCreatedAtDesc(
                userId,
                status,
                pageable
        );
    }

    public Order findById(Long orderId) {
        return orderRepository.findById(orderId).orElse(null);
    }

    public Order findUserOrder(
            Long orderId,
            Long userId
    ) {
        return orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElse(null);
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    public Page<Order> getOrdersByStatus(
            OrderStatus status,
            Pageable pageable
    ) {
        return orderRepository.findByStatusOrderByCreatedAtDesc(
                status,
                pageable
        );
    }

    @Transactional
    public Order createOrder(
            Long userId,
            String recipientName,
            String recipientPhone,
            String shippingAddress,
            String discountCode,
            BigDecimal discountAmount
    ) {
        if (userId == null) {
            return null;
        }

        if (recipientName == null || recipientName.trim().isEmpty()) {
            return null;
        }

        if (recipientPhone == null || recipientPhone.trim().isEmpty()) {
            return null;
        }

        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            return null;
        }

        User user = userRepository
                .findById(userId)
                .orElse(null);

        if (user == null) {
            return null;
        }

        Cart cart = cartRepository
                .findByUserId(userId)
                .orElse(null);

        if (cart == null || cart.getItems().isEmpty()) {
            return null;
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            if (product == null) {
                return null;
            }

            if (!Boolean.TRUE.equals(product.getStatus())) {
                return null;
            }

            if (product.getStock() < cartItem.getQuantity()) {
                return null;
            }

            BigDecimal itemTotal = product.getPrice()
                    .multiply(
                            BigDecimal.valueOf(
                                    cartItem.getQuantity()
                            )
                    );

            subtotal = subtotal.add(itemTotal);
        }

        if (discountAmount == null
                || discountAmount.compareTo(BigDecimal.ZERO) < 0) {
            discountAmount = BigDecimal.ZERO;
        }

        if (discountAmount.compareTo(subtotal) > 0) {
            discountAmount = subtotal;
        }

        BigDecimal finalAmount = subtotal.subtract(discountAmount);

        Order order = Order.builder()
                .user(user)
                .recipientName(recipientName.trim())
                .recipientPhone(recipientPhone.trim())
                .shippingAddress(shippingAddress.trim())
                .subtotal(subtotal)
                .discountCode(discountCode != null && !discountCode.isBlank() ? discountCode.trim().toUpperCase() : null)
                .discountAmount(discountAmount)
                .finalAmount(finalAmount)
                .status(OrderStatus.PENDING)
                .build();

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            OrderItem orderItem = OrderItem.builder()
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .price(product.getPrice())
                    .build();

            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);

        for (CartItem cartItem : cart.getItems()) {

            Product product = cartItem.getProduct();

            int newStock =
                    product.getStock() - cartItem.getQuantity();

            if (newStock < 0) {
                return null;
            }

            product.setStock(newStock);
            productRepository.save(product);
        }

        cart.getItems().clear();
        cartRepository.save(cart);

        return savedOrder;
    }

    @Transactional
    public Order createOrder(
            Long userId,
            String recipientName,
            String recipientPhone,
            String shippingAddress,
            BigDecimal discountAmount
    ) {
        return createOrder(userId, recipientName, recipientPhone, shippingAddress, null, discountAmount);
    }

    @Transactional
    public boolean cancelOrder(
            Long orderId,
            Long userId
    ) {
        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElse(null);

        if (order == null) {
            return false;
        }

        if (order.getStatus() != OrderStatus.PENDING) {
            return false;
        }

        order.setStatus(OrderStatus.CANCELLED);

        orderRepository.save(order);

        restoreStock(order);

        return true;
    }

    @Transactional
    public boolean requestReturn(
            Long orderId,
            Long userId
    ) {
        Order order = orderRepository
                .findByIdAndUserId(orderId, userId)
                .orElse(null);

        if (order == null) {
            return false;
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            return false;
        }

        order.setStatus(OrderStatus.RETURN_REQUESTED);

        orderRepository.save(order);

        return true;
    }

    @Transactional
    public boolean updateStatus(
            Long orderId,
            OrderStatus newStatus
    ) {
        if (newStatus == null) {
            return false;
        }

        Order order = orderRepository
                .findById(orderId)
                .orElse(null);

        if (order == null) {
            return false;
        }

        OrderStatus currentStatus = order.getStatus();

        if (!isValidTransition(currentStatus, newStatus)) {
            return false;
        }

        order.setStatus(newStatus);

        orderRepository.save(order);

        if (newStatus == OrderStatus.CANCELLED || newStatus == OrderStatus.REFUNDED) {
            restoreStock(order);
        }

        return true;
    }

    private boolean isValidTransition(
            OrderStatus currentStatus,
            OrderStatus newStatus
    ) {
        if (currentStatus == null || newStatus == null) {
            return false;
        }

        return switch (currentStatus) {

            case PENDING ->
                    newStatus == OrderStatus.CONFIRMED
                            || newStatus == OrderStatus.CANCELLED;

            case CONFIRMED ->
                    newStatus == OrderStatus.SHIPPING
                            || newStatus == OrderStatus.CANCELLED;

            case SHIPPING ->
                    newStatus == OrderStatus.DELIVERED;

            case DELIVERED ->
                    newStatus == OrderStatus.RETURN_REQUESTED;

            case RETURN_REQUESTED ->
                    newStatus == OrderStatus.REFUNDED;

            case CANCELLED, REFUNDED ->
                    false;
        };
    }

    private void restoreStock(Order order) {
        if (order.getItems() == null) {
            return;
        }

        for (OrderItem orderItem : order.getItems()) {

            Product product = orderItem.getProduct();

            if (product == null) {
                continue;
            }

            product.setStock(
                    product.getStock()
                            + orderItem.getQuantity()
            );

            productRepository.save(product);
        }
    }
}
