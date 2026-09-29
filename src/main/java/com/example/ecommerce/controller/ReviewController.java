package com.example.ecommerce.controller;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.enums.OrderStatus;
import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final OrderService orderService;
    private final ProductService productService;

    @GetMapping("/create")
    public String createForm(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long orderId,
            @RequestParam Long productId,
            Model model,
            RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getUserId();

        Order order =
                orderService.findUserOrder(
                        orderId,
                        userId
                );

        if (order == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Không tìm thấy đơn hàng."
            );
            return "redirect:/orders";
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã giao."
            );

            return "redirect:/orders/" + orderId;
        }

        boolean purchased =
                order.getItems()
                        .stream()
                        .anyMatch(item ->
                                item.getProduct()
                                        .getId()
                                        .equals(productId)
                        );

        if (!purchased) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Sản phẩm không thuộc đơn hàng này."
            );

            return "redirect:/orders/" + orderId;
        }

        if (reviewService.hasReviewed(
                userId,
                productId,
                orderId)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Bạn đã đánh giá sản phẩm này trong đơn hàng."
            );

            return "redirect:/orders/" + orderId;
        }

        model.addAttribute(
                "product",
                productService.findById(productId)
        );

        model.addAttribute("orderId", orderId);
        model.addAttribute("productId", productId);

        return "review/form";
    }

    @PostMapping("/create")
    public String createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam Long orderId,
            @RequestParam Long productId,
            @RequestParam Integer rating,
            @RequestParam String comment,
            RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getUserId();

        Order order =
                orderService.findUserOrder(
                        orderId,
                        userId
                );

        if (order == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Không tìm thấy đơn hàng."
            );
            return "redirect:/orders";
        }

        if (order.getStatus() != OrderStatus.DELIVERED) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Đơn hàng chưa ở trạng thái đã giao."
            );

            return "redirect:/orders/" + orderId;
        }

        boolean purchased =
                order.getItems()
                        .stream()
                        .anyMatch(item ->
                                item.getProduct()
                                        .getId()
                                        .equals(productId)
                        );

        if (!purchased) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Bạn chưa mua sản phẩm này trong đơn hàng."
            );

            return "redirect:/orders/" + orderId;
        }

        if (reviewService.hasReviewed(
                userId,
                productId,
                orderId)) {

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Bạn đã đánh giá sản phẩm này."
            );

            return "redirect:/orders/" + orderId;
        }

        if (rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Rating phải từ 1 đến 5 sao."
            );

            return "redirect:/orders/" + orderId;
        }

        reviewService.createReview(
                userId,
                productId,
                orderId,
                rating,
                comment
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Đánh giá sản phẩm thành công."
        );

        return "redirect:/products/" + productId;
    }
}
