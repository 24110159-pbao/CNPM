package com.example.ecommerce.controller.manager;

import com.example.ecommerce.entity.Notification;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.enums.PaymentMethod;
import com.example.ecommerce.enums.NotificationType;
import com.example.ecommerce.service.NotificationService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.PaymentService;
import com.example.ecommerce.service.WebSocketNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/manager/orders")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerOrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final NotificationService notificationService;
    private final WebSocketNotificationService webSocketNotificationService;

    @GetMapping
    public String list(
            @RequestParam(required = false) OrderStatus status,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        // Nếu có khoảng ngày thì dùng filter ngày (có thể kết hợp status)
        if (startDate != null && endDate != null) {
            model.addAttribute(
                    "orders",
                    orderService.getOrdersByDateRange(status, startDate, endDate, pageable)
            );
        } else if (status != null) {
            model.addAttribute(
                    "orders",
                    orderService.getOrdersByStatus(status, pageable)
            );
        } else {
            model.addAttribute(
                    "orders",
                    orderService.getAllOrders(pageable)
            );
        }

        model.addAttribute("statuses", OrderStatus.values());
        model.addAttribute("selectedStatus", status);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);

        return "manager/orders/list";
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            Model model) {

        Order order = orderService.findById(id);

        model.addAttribute("order", order);

        model.addAttribute(
                "payment",
                paymentService.findByOrderId(id)
        );

        return "manager/orders/detail";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(
            @PathVariable Long id,
            @RequestParam OrderStatus newStatus,
            RedirectAttributes redirectAttributes) {

        Order order = orderService.findById(id);

        orderService.updateStatus(
                id,
                newStatus
        );

        String title;
        String message;

        switch (newStatus) {
            case CONFIRMED -> {
                title = "Đơn hàng đã được xác nhận";
                message = "Đơn hàng " + order.getId()
                        + " đã được xác nhận.";
            }

            case SHIPPING -> {
                title = "Đơn hàng đang được giao";
                message = "Đơn hàng " + order.getId()
                        + " đang được giao.";
            }

            case DELIVERED -> {
                title = "Đơn hàng đã giao thành công";
                message = "Đơn hàng " + order.getId()
                        + " đã được giao thành công.";
            }

            case CANCELLED -> {
                title = "Đơn hàng đã bị hủy";
                message = "Đơn hàng " + order.getId()
                        + " đã bị hủy.";
            }

            case RETURN_REQUESTED -> {
                title = "Yêu cầu trả hàng";
                message = "Đơn hàng " + order.getId()
                        + " đang có yêu cầu trả hàng.";
            }

            case REFUNDED -> {
                title = "Đơn hàng đã được hoàn tiền";
                message = "Đơn hàng " + order.getId()
                        + " đã được hoàn tiền.";
            }

            default -> {
                title = "Cập nhật đơn hàng";
                message = "Trạng thái đơn hàng " + order.getId()
                        + " đã được cập nhật.";
            }
        }

        Notification notification =
                notificationService.createNotification(
                        order.getUser().getId(),
                        title,
                        message,
                        NotificationType.ORDER_STATUS
                );

        webSocketNotificationService.sendOrderNotification(
                order.getUser().getId(),
                notification
        );

        if (newStatus == OrderStatus.DELIVERED
                && order.getPayment() != null
                && order.getPayment().getMethod() == PaymentMethod.COD) {

            paymentService.markCodAsPaid(id);
        }

        if (newStatus == OrderStatus.REFUNDED) {
            paymentService.refund(id);
        }

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã cập nhật trạng thái đơn hàng."
        );

        return "redirect:/manager/orders/" + id;
    }
}
