package com.example.ecommerce.controller;

import com.example.ecommerce.service.PaymentService;
import com.example.ecommerce.service.VnPayResult;
import com.example.ecommerce.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VnPayService vnPayService;
    private final PaymentService paymentService;

    @GetMapping("/vnpay-return")
    public String vnpayReturn(
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        VnPayResult result =
                vnPayService.verifyReturn(request);

        if (result.isValid()) {

            if (!paymentService.handleVnPayResult(result)) {
                return "redirect:/orders/payment-result?status=invalid";
            }

            if (!result.isSuccess()) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Thanh toán đã bị hủy hoặc thất bại. Hãy chọn lại phương thức thanh toán."
                );
                return "redirect:/orders/" + result.getOrderId()
                        + "/payment-method";
            }

            return "redirect:/orders/"
                    + result.getOrderId();
        }

        return "redirect:/orders/payment-result?status=invalid";
    }
}
