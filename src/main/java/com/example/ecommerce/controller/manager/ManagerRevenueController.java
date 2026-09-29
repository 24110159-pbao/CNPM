package com.example.ecommerce.controller.manager;

import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Controller
@RequestMapping("/manager/revenue")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerRevenueController {

    private final OrderRepository orderRepository;

    @GetMapping
    public String revenue(
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            Model model) {

        LocalDate today = LocalDate.now();

        if (startDate == null) {
            startDate = today;
        }

        if (endDate == null) {
            endDate = today;
        }

        LocalDateTime startDateTime =
                startDate.atStartOfDay();

        LocalDateTime endDateTime =
                endDate.atTime(LocalTime.MAX);

        BigDecimal revenue =
                orderRepository
                        .sumFinalAmountByStatusAndCreatedAtBetween(
                                OrderStatus.DELIVERED,
                                startDateTime,
                                endDateTime
                        );

        BigDecimal totalRevenue =
                orderRepository
                        .sumFinalAmountByStatus(
                                OrderStatus.DELIVERED
                        );

        long totalOrdersInRange =
                orderRepository
                        .countByCreatedAtBetween(
                                startDateTime,
                                endDateTime
                        );

        long deliveredOrdersInRange =
                orderRepository
                        .countByStatusAndCreatedAtBetween(
                                OrderStatus.DELIVERED,
                                startDateTime,
                                endDateTime
                        );

        long cancelledOrdersInRange =
                orderRepository
                        .countByStatusAndCreatedAtBetween(
                                OrderStatus.CANCELLED,
                                startDateTime,
                                endDateTime
                        );

        model.addAttribute(
                "startDate",
                startDate
        );

        model.addAttribute(
                "endDate",
                endDate
        );

        model.addAttribute(
                "revenue",
                revenue
        );

        model.addAttribute(
                "totalRevenue",
                totalRevenue
        );

        model.addAttribute(
                "totalOrdersInRange",
                totalOrdersInRange
        );

        model.addAttribute(
                "deliveredOrdersInRange",
                deliveredOrdersInRange
        );

        model.addAttribute(
                "cancelledOrdersInRange",
                cancelledOrdersInRange
        );

        return "manager/revenue/index";
    }
}
