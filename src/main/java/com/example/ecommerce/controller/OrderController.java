package com.example.ecommerce.controller;

import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;

    @GetMapping
    public String orders(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) OrderStatus status,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        Long userId = userDetails.getUserId();

        if (status != null) {
            model.addAttribute(
                    "orders",
                    orderService.getUserOrdersByStatus(
                            userId,
                            status,
                            pageable
                    )
            );
        } else {
            model.addAttribute(
                    "orders",
                    orderService.getUserOrders(
                            userId,
                            pageable
                    )
            );
        }

        model.addAttribute("selectedStatus", status);
        model.addAttribute("statuses", OrderStatus.values());

        return "order/list";
    }

    @GetMapping("/{id}")
    public String orderDetail(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            Model model) {

        Long userId = userDetails.getUserId();

        model.addAttribute(
                "order",
                orderService.findUserOrder(id, userId)
        );

        model.addAttribute(
                "payment",
                paymentService.findByOrderId(id)
        );

        return "order/detail";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        orderService.cancelOrder(
                id,
                userDetails.getUserId()
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã yêu cầu hủy đơn hàng."
        );

        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/return")
    public String requestReturn(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        orderService.requestReturn(
                id,
                userDetails.getUserId()
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã gửi yêu cầu trả hàng/hoàn tiền."
        );

        return "redirect:/orders/" + id;
    }

    @GetMapping("/payment-result")
    public String paymentResult(
            @RequestParam(required = false) String status,
            Model model) {

        model.addAttribute("status", status);

        return "order/payment-result";
    }
}
