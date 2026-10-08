package com.example.ecommerce.service;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InvoicePdfService {

    private final OrderRepository orderRepository;
    private final TemplateEngine templateEngine;

    @Transactional(readOnly = true)
    public byte[] generateInvoicePdf(
            Long orderId,
            Authentication authentication) {

        // 1. Tìm đơn hàng
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy đơn hàng"));

        // 2. Kiểm tra quyền
        checkOrderPermission(order, authentication);

        // 3. Lấy danh sách sản phẩm
        List<OrderItem> orderItems = order.getOrderItems();

        // 4. Tạo Thymeleaf context
        Context context = new Context();

        context.setVariable("order", order);
        context.setVariable("orderItems", orderItems);

        // 5. Render HTML
        String html = templateEngine.process(
                "invoice/invoice",
                context
        );

        // 6. Chuyển HTML thành PDF
        try (ByteArrayOutputStream outputStream =
                     new ByteArrayOutputStream()) {

            ITextRenderer renderer = new ITextRenderer();

            renderer.setDocumentFromString(html);

            renderer.layout();

            renderer.createPDF(outputStream);

            return outputStream.toByteArray();

        } catch (Exception e) {

            throw new RuntimeException(
                    "Không thể tạo hóa đơn PDF",
                    e
            );
        }
    }

    private void checkOrderPermission(
            Order order,
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()) {

            throw new RuntimeException(
                    "Bạn chưa đăng nhập"
            );
        }

        // Manager được in tất cả đơn hàng
        boolean isManager = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority()
                                .equals("ROLE_MANAGER")
                );

        if (isManager) {
            return;
        }

        // User chỉ được in đơn hàng của mình
        String username = authentication.getName();

        if (order.getUser() == null
                || order.getUser().getEmail() == null
                || !order.getUser()
                .getEmail()
                .equalsIgnoreCase(username)) {

            throw new RuntimeException(
                    "Bạn không có quyền in hóa đơn này"
            );
        }
    }
}