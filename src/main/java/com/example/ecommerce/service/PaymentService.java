package com.example.ecommerce.service;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.enums.PaymentMethod;
import com.example.ecommerce.enums.PaymentStatus;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;

    public Payment findByOrderId(Long orderId) {
        return paymentRepository
                .findByOrderId(orderId)
                .orElse(null);
    }

    @Transactional
    public Payment createPayment(
            Order order,
            PaymentMethod method
    ) {
        if (order == null || method == null) {
            return null;
        }

        if (paymentRepository
                .findByOrderId(order.getId())
                .isPresent()) {
            return null;
        }

        PaymentStatus status =
                method == PaymentMethod.COD
                        ? PaymentStatus.PENDING
                        : PaymentStatus.PENDING;

        Payment payment = Payment.builder()
                .order(order)
                .method(method)
                .status(status)
                .transactionNo(null)
                .paidAt(null)
                .build();

        return paymentRepository.save(payment);
    }

    @Transactional
    public boolean markCodAsPaid(
            Long orderId
    ) {
        Payment payment =
                paymentRepository
                        .findByOrderId(orderId)
                        .orElse(null);

        if (payment == null) {
            return false;
        }

        if (payment.getMethod()
                != PaymentMethod.COD) {
            return false;
        }

        payment.setStatus(
                PaymentStatus.PAID
        );

        payment.setPaidAt(
                LocalDateTime.now()
        );

        paymentRepository.save(payment);

        return true;
    }

    @Transactional
    public boolean handleVnPayResult(
            VnPayResult result
    ) {
        if (result == null
                || !result.isValid()) {
            return false;
        }

        Long orderId = result.getOrderId();

        Payment payment =
                paymentRepository
                        .findByOrderId(orderId)
                        .orElse(null);

        if (payment == null) {
            return false;
        }

        if (payment.getMethod()
                != PaymentMethod.VNPAY) {
            return false;
        }

        if (payment.getStatus() != PaymentStatus.PENDING
                || payment.getTransactionNo() == null
                || !payment.getTransactionNo().equals(result.getTransactionNo())) {
            return false;
        }

        payment.setTransactionNo(
                result.getTransactionNo()
        );

        if (result.isSuccess()) {

            payment.setStatus(
                    PaymentStatus.PAID
            );

            payment.setPaidAt(
                    LocalDateTime.now()
            );

        } else {

            payment.setStatus(
                    PaymentStatus.FAILED
            );

        }

        paymentRepository.save(payment);

        return true;
    }

    @Transactional
    public boolean resetUnpaidPayment(
            Long orderId,
            PaymentMethod method
    ) {
        if (orderId == null || method == null) {
            return false;
        }

        Payment payment = paymentRepository
                .findByOrderId(orderId)
                .orElse(null);

        if (payment == null
                || payment.getStatus() == PaymentStatus.PAID
                || payment.getStatus() == PaymentStatus.REFUNDED) {
            return false;
        }

        payment.setMethod(method);
        payment.setStatus(PaymentStatus.PENDING);
        payment.setTransactionNo(null);
        payment.setPaidAt(null);
        paymentRepository.save(payment);
        return true;
    }

    @Transactional
    public boolean refund(
            Long orderId
    ) {
        Payment payment =
                paymentRepository
                        .findByOrderId(orderId)
                        .orElse(null);

        if (payment == null) {
            return false;
        }

        if (payment.getStatus()
                != PaymentStatus.PAID) {
            return false;
        }

        payment.setStatus(
                PaymentStatus.REFUNDED
        );

        paymentRepository.save(payment);

        return true;
    }
}
