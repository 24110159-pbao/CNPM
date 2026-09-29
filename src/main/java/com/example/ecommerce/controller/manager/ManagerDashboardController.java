package com.example.ecommerce.controller.manager;

import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.UserService;
import com.example.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/manager/dashboard")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerDashboardController {

    private final UserService userService;
    private final ProductService productService;
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @GetMapping
    public String dashboard(Model model) {

        model.addAttribute(
                "totalUsers",
                userService.getAllUsers(
                        org.springframework.data.domain.PageRequest.of(0, 1)
                ).getTotalElements()
        );

        model.addAttribute(
                "totalProducts",
                productService.getAllProducts(
                        org.springframework.data.domain.PageRequest.of(0, 1)
                ).getTotalElements()
        );

        model.addAttribute(
                "totalOrders",
                orderService.getAllOrders(
                        org.springframework.data.domain.PageRequest.of(0, 1)
                ).getTotalElements()
        );

        for (OrderStatus status : OrderStatus.values()) {
            model.addAttribute(
                    "orderCount_" + status.name(),
                    orderRepository.countByStatus(status)
            );
        }

        model.addAttribute(
                "revenue",
                orderRepository.sumFinalAmountByStatus(
                        OrderStatus.DELIVERED
                )
        );

        return "manager/dashboard";
    }
}
