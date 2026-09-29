package com.example.ecommerce.controller;

import com.example.ecommerce.service.PaymentService;
import com.example.ecommerce.service.VnPayResult;
import com.example.ecommerce.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final VnPayService vnPayService;
    private final PaymentService paymentService;

    @GetMapping("/vnpay-return")
    public String vnpayReturn(
            HttpServletRequest request) {

        VnPayResult result =
                vnPayService.verifyReturn(request);

        if (result.isValid()) {

            paymentService.handleVnPayResult(result);

            return "redirect:/orders/"
                    + result.getOrderId();
        }

        return "redirect:/orders/payment-result?status=invalid";
    }
}
