package com.example.ecommerce.payment.vnpay;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "vnpay")
public class VnPayConfig {

    private String tmnCode;

    private String hashSecret;

    private String paymentUrl;

    private String returnUrl;

    private String version = "2.1.0";

    private String command = "pay";

    private String orderType = "other";

    private String currCode = "VND";

    private String locale = "vn";
}
