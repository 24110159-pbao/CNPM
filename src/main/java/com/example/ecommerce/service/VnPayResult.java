package com.example.ecommerce.service;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VnPayResult {

    private boolean valid;

    private boolean success;

    private Long orderId;

    private String transactionNo;

    private String amount;

    private String responseCode;

    public static VnPayResult invalid() {
        return VnPayResult.builder()
                .valid(false)
                .success(false)
                .build();
    }
}
