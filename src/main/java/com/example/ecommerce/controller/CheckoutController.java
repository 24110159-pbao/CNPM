package com.example.ecommerce.controller;

import com.example.ecommerce.dto.CheckoutRequest;
import com.example.ecommerce.entity.DiscountCode;
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.enums.PaymentMethod;
import com.example.ecommerce.security.CustomUserDetails;
import com.example.ecommerce.service.CartService;
import com.example.ecommerce.service.DiscountService;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.PaymentService;
import com.example.ecommerce.service.UserService;
import com.example.ecommerce.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor
public class CheckoutController {

    private final CartService cartService;
    private final UserService userService;
    private final DiscountService discountService;
    private final OrderService orderService;
    private final PaymentService paymentService;
    private final VnPayService vnPayService;

    @GetMapping
    public String checkout(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            Model model) {

        Long userId = userDetails.getUserId();

        User user = userService.findById(userId);

        model.addAttribute(
                "cart",
                cartService.getOrCreateCart(userId)
        );

        model.addAttribute("user", user);
        model.addAttribute("checkoutRequest", new CheckoutRequest());

        return "checkout/checkout";
    }

    @PostMapping("/place-order")
    public String placeOrder(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @ModelAttribute CheckoutRequest request,
            HttpServletRequest httpRequest,
            RedirectAttributes redirectAttributes) {

        Long userId = userDetails.getUserId();

        var cart = cartService.getOrCreateCart(userId);

        if (cart == null || cart.getItems() == null || cart.getItems().isEmpty()) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Giỏ hàng của bạn đang trống."
            );
            return "redirect:/cart";
        }

        BigDecimal subtotal = calculateSubtotal(cart);

        BigDecimal discountAmount = BigDecimal.ZERO;

        DiscountCode discount = null;

        if (request.getDiscountCode() != null
                && !request.getDiscountCode().isBlank()) {

            discount =
                    discountService.findByCode(
                            request.getDiscountCode()
                    );

            if (discount == null
                    || !discountService.isValid(discount)) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "Mã giảm giá không hợp lệ hoặc đã hết hạn."
                );

                return "redirect:/checkout";
            }

            discountAmount =
                    discountService.calculateDiscount(
                            discount.getCode(),
                            subtotal
                    );
        }

        String discountCodeStr = (discount != null) ? discount.getCode() : null;

        Order order =
                orderService.createOrder(
                        userId,
                        request.getRecipientName(),
                        request.getRecipientPhone(),
                        request.getShippingAddress(),
                        discountCodeStr,
                        discountAmount
                );

        if (order == null) {
            redirectAttributes.addFlashAttribute(
                    "error",
                    "Đặt hàng thất bại. Vui lòng kiểm tra lại tồn kho sản phẩm hoặc thông tin giao hàng."
            );
            return "redirect:/checkout";
        }

        paymentService.createPayment(
                order,
                request.getPaymentMethod()
        );

        if (discount != null) {
            discountService.decreaseQuantity(
                    discount.getId()
            );
        }

        if (request.getPaymentMethod() == PaymentMethod.VNPAY) {

            String paymentUrl =
                    vnPayService.createPaymentUrl(
                            order,
                            httpRequest
                    );

            if (paymentUrl != null) {
                return "redirect:" + paymentUrl;
            }

            redirectAttributes.addFlashAttribute(
                    "error",
                    "Không tạo được liên kết thanh toán. Vui lòng chọn lại phương thức thanh toán."
            );
            return "redirect:/orders/" + order.getId()
                    + "/payment-method";
        }

        redirectAttributes.addFlashAttribute(
                "success",
                "Đặt hàng thành công."
        );

        return "redirect:/orders/" + order.getId();
    }

    private BigDecimal calculateSubtotal(com.example.ecommerce.entity.Cart cart) {
        if (cart == null || cart.getItems() == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = BigDecimal.ZERO;
        for (com.example.ecommerce.entity.CartItem item : cart.getItems()) {
            if (item.getProduct() != null && item.getProduct().getPrice() != null && item.getQuantity() != null) {
                BigDecimal itemTotal = item.getProduct().getPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity()));
                subtotal = subtotal.add(itemTotal);
            }
        }
        return subtotal;
    }
}
