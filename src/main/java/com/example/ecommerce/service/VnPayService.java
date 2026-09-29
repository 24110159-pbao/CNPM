package com.example.ecommerce.service;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.enums.PaymentMethod;
import com.example.ecommerce.enums.PaymentStatus;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;
import com.example.ecommerce.payment.vnpay.VnPayConfig;
import com.example.ecommerce.payment.vnpay.VnPayUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class VnPayService {

    private final VnPayConfig vnPayConfig;
    private final VnPayUtil vnPayUtil;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    public String createPaymentUrl(
            Order order,
            HttpServletRequest request
    ) {
        if (order == null
                || request == null) {
            return null;
        }

        Payment payment =
                paymentRepository
                        .findByOrderId(order.getId())
                        .orElse(null);

        if (payment == null) {
            return null;
        }

        if (payment.getMethod()
                != PaymentMethod.VNPAY) {
            return null;
        }

        if (payment.getStatus()
                != PaymentStatus.PENDING) {
            return null;
        }

        BigDecimal amount =
                order.getFinalAmount();

        if (amount == null
                || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return null;
        }

        String transactionRef =
                "ORDER" + order.getId()
                        + "_" + System.currentTimeMillis();

        payment.setTransactionNo(transactionRef);

        paymentRepository.save(payment);

        return vnPayUtil.buildPaymentUrl(
                vnPayConfig,
                request,
                amount,
                transactionRef,
                "Thanh toan don hang #" + order.getId()
        );
    }

    public VnPayResult verifyReturn(
            HttpServletRequest request
    ) {
        if (request == null) {
            return VnPayResult.invalid();
        }

        Map<String, String> params =
                vnPayUtil.getRequestParams(request);

        if (params.isEmpty()) {
            return VnPayResult.invalid();
        }

        boolean valid =
                vnPayUtil.verifySignature(
                        vnPayConfig,
                        params
                );

        if (!valid) {
            return VnPayResult.invalid();
        }

        String responseCode =
                params.get("vnp_ResponseCode");

        String transactionStatus =
                params.get("vnp_TransactionStatus");

        String transactionNo =
                params.get("vnp_TxnRef");

        String amount =
                params.get("vnp_Amount");

        if (transactionNo == null
                || transactionNo.isBlank()) {
            return VnPayResult.invalid();
        }

        Long orderId =
                extractOrderId(transactionNo);

        if (orderId == null) {
            return VnPayResult.invalid();
        }

        Order order =
                orderRepository
                        .findById(orderId)
                        .orElse(null);

        if (order == null) {
            return VnPayResult.invalid();
        }

        Payment payment =
                paymentRepository
                        .findByOrderId(orderId)
                        .orElse(null);

        if (payment == null) {
            return VnPayResult.invalid();
        }

        if (payment.getMethod()
                != PaymentMethod.VNPAY) {
            return VnPayResult.invalid();
        }

        boolean success =
                "00".equals(responseCode)
                        && "00".equals(transactionStatus);

        return VnPayResult.builder()
                .valid(true)
                .success(success)
                .orderId(orderId)
                .transactionNo(transactionNo)
                .amount(amount)
                .responseCode(responseCode)
                .build();
    }

    private Long extractOrderId(
            String transactionRef
    ) {
        try {
            if (!transactionRef.startsWith("ORDER")) {
                return null;
            }

            String value =
                    transactionRef.substring(
                            "ORDER".length()
                    );

            int separator =
                    value.indexOf("_");

            if (separator >= 0) {
                value =
                        value.substring(
                                0,
                                separator
                        );
            }

            return Long.parseLong(value);

        } catch (NumberFormatException e) {
            return null;
        }
    }
}
