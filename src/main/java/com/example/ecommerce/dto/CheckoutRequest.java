package com.example.ecommerce.dto;

import com.example.ecommerce.enums.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutRequest {

    @NotBlank(message = "Vui lòng nhập tên người nhận")
    private String recipientName;

    @NotBlank(message = "Vui lòng nhập số điện thoại")
    private String recipientPhone;

    @NotBlank(message = "Vui lòng nhập địa chỉ nhận hàng")
    private String shippingAddress;

    private String discountCode;

    @NotNull(message = "Vui lòng chọn phương thức thanh toán")
    private PaymentMethod paymentMethod;
}
