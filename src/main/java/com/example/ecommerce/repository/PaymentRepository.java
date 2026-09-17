package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PaymentRepository
        extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    Optional<Payment> findByTransactionNo(String transactionNo);

    boolean existsByOrderId(Long orderId);
}
