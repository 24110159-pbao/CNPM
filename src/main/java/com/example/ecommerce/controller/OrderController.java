package com.example.ecommerce.controller;

import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.enums.NotificationType;
import com.example.ecommerce.enums.PaymentMethod;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.enums.PaymentStatus;
import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.service.NotificationService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.PaymentService;
import com.example.ecommerce.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
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
        private final NotificationService notificationService;
        private final VnPayService vnPayService;

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

        Order order = orderService.findUserOrder(
                id,
                userDetails.getUserId()
        );

        boolean cancelled = orderService.cancelOrder(
                id,
                userDetails.getUserId()
        );

        if (!cancelled || order == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Không thể hủy đơn hàng này."
            );
            return "redirect:/orders/" + id;
        }

        notificationService.notifyManagers(
                "Khách hàng yêu cầu hủy đơn",
                "Đơn hàng #" + order.getId() + " của "
                        + order.getUser().getName() + " ("
                        + order.getUser().getEmail() + ") đã được hủy.",
                NotificationType.ORDER_REQUEST
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Đơn hàng đã được hủy."
        );

        return "redirect:/orders/" + id;
    }

    @PostMapping("/{id}/return")
    public String requestReturn(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestParam String bankName,
            @RequestParam String bankAccountNumber,
            RedirectAttributes redirectAttributes) {

        if (bankName.isBlank() || bankName.length() > 100
                || !bankAccountNumber.matches("[0-9]{6,30}")) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Vui lòng nhập tên ngân hàng và số tài khoản hợp lệ."
            );
            return "redirect:/orders/" + id;
        }

        Order order = orderService.findUserOrder(
                id,
                userDetails.getUserId()
        );

        boolean requested = orderService.requestReturn(
                id,
                userDetails.getUserId()
        );

        if (!requested || order == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Không thể gửi yêu cầu trả hàng/hoàn tiền cho đơn này."
            );
            return "redirect:/orders/" + id;
        }

        notificationService.notifyManagers(
                "Yêu cầu trả hàng / hoàn tiền",
                "Đơn hàng #" + order.getId() + " của "
                        + order.getUser().getName() + " ("
                        + order.getUser().getEmail() + "). Ngân hàng: "
                        + bankName.trim() + "; số tài khoản: "
                        + bankAccountNumber + ".",
                NotificationType.ORDER_REQUEST
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã gửi yêu cầu trả hàng/hoàn tiền."
        );

        return "redirect:/orders/" + id;
    }

    @GetMapping("/{id}/payment-method")
    public String paymentMethodForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            Model model,
            RedirectAttributes redirectAttributes) {

        Order order = orderService.findUserOrder(
                id,
                userDetails.getUserId()
        );
        Payment payment = paymentService.findByOrderId(id);

        if (!canChoosePaymentMethod(order, payment)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Đơn hàng này không thể thay đổi phương thức thanh toán."
            );
            return "redirect:/orders/" + id;
        }

        model.addAttribute("order", order);
        model.addAttribute("payment", payment);
        return "order/payment-method";
    }

    @PostMapping("/{id}/payment-method")
    public String changePaymentMethod(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestParam PaymentMethod paymentMethod,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Order order = orderService.findUserOrder(
                id,
                userDetails.getUserId()
        );
        Payment payment = paymentService.findByOrderId(id);

        if (!canChoosePaymentMethod(order, payment)
                || !paymentService.resetUnpaidPayment(id, paymentMethod)) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Không thể thay đổi phương thức thanh toán cho đơn này."
            );
            return "redirect:/orders/" + id;
        }

        if (paymentMethod == PaymentMethod.VNPAY) {
            String paymentUrl = vnPayService.createPaymentUrl(order, request);
            if (paymentUrl != null) {
                return "redirect:" + paymentUrl;
            }

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Không tạo được liên kết thanh toán. Vui lòng thử lại hoặc chọn COD."
            );
            return "redirect:/orders/" + id + "/payment-method";
        }

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã cập nhật phương thức thanh toán sang COD."
        );
        return "redirect:/orders/" + id;
    }

    private boolean canChoosePaymentMethod(Order order, Payment payment) {
        return order != null
                && order.getStatus() == OrderStatus.PENDING
                && payment != null
                && payment.getStatus() != PaymentStatus.PAID
                && payment.getStatus() != PaymentStatus.REFUNDED;
    }

    @GetMapping("/payment-result")
    public String paymentResult(
            @RequestParam(required = false) String status,
            Model model) {

        model.addAttribute("status", status);

        return "order/payment-result";
    }
}
