package com.example.ecommerce.payment.vnpay;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

@Component
public class VnPayUtil {

    public String buildPaymentUrl(
            VnPayConfig config,
            HttpServletRequest request,
            BigDecimal amount,
            String transactionRef,
            String orderInfo
    ) {
        if (config == null
                || request == null
                || amount == null
                || transactionRef == null
                || orderInfo == null) {
            return null;
        }

        long amountValue =
                amount
                        .multiply(BigDecimal.valueOf(100))
                        .longValue();

        Map<String, String> params =
                new HashMap<>();

        params.put(
                "vnp_Version",
                config.getVersion()
        );

        params.put(
                "vnp_Command",
                config.getCommand()
        );

        params.put(
                "vnp_TmnCode",
                config.getTmnCode()
        );

        params.put(
                "vnp_Amount",
                String.valueOf(amountValue)
        );

        params.put(
                "vnp_CurrCode",
                config.getCurrCode()
        );

        params.put(
                "vnp_TxnRef",
                transactionRef
        );

        params.put(
                "vnp_OrderInfo",
                orderInfo
        );

        params.put(
                "vnp_OrderType",
                config.getOrderType()
        );

        params.put(
                "vnp_Locale",
                config.getLocale()
        );

        params.put(
                "vnp_ReturnUrl",
                config.getReturnUrl()
        );

        params.put(
                "vnp_IpAddr",
                getClientIpAddress(request)
        );

        params.put(
                "vnp_CreateDate",
                getCurrentDate()
        );

        params.put(
                "vnp_ExpireDate",
                getExpireDate()
        );

        String queryString =
                buildQueryString(params);

        String secureHash =
                hmacSHA512(
                        config.getHashSecret(),
                        queryString
                );

        return config.getPaymentUrl()
                + "?"
                + queryString
                + "&vnp_SecureHash="
                + secureHash;
    }

    public Map<String, String> getRequestParams(
            HttpServletRequest request
    ) {
        Map<String, String> params =
                new HashMap<>();

        request.getParameterMap()
                .forEach((key, values) -> {

                    if (values != null
                            && values.length > 0) {

                        params.put(
                                key,
                                values[0]
                        );
                    }
                });

        return params;
    }

    public boolean verifySignature(
            VnPayConfig config,
            Map<String, String> params
    ) {
        if (config == null
                || params == null
                || params.isEmpty()) {
            return false;
        }

        String receivedHash =
                params.get("vnp_SecureHash");

        if (receivedHash == null
                || receivedHash.isBlank()) {
            return false;
        }

        Map<String, String> filteredParams =
                new HashMap<>();

        for (Map.Entry<String, String> entry
                : params.entrySet()) {

            String key = entry.getKey();

            if ("vnp_SecureHash".equals(key)
                    || "vnp_SecureHashType".equals(key)) {
                continue;
            }

            filteredParams.put(
                    key,
                    entry.getValue()
            );
        }

        String queryString =
                buildQueryString(filteredParams);

        String calculatedHash =
                hmacSHA512(
                        config.getHashSecret(),
                        queryString
                );

        return calculatedHash.equalsIgnoreCase(
                receivedHash
        );
    }

    private String buildQueryString(
            Map<String, String> params
    ) {
        List<String> fieldNames =
                new ArrayList<>(params.keySet());

        Collections.sort(fieldNames);

        StringBuilder query =
                new StringBuilder();

        for (String fieldName : fieldNames) {

            String value =
                    params.get(fieldName);

            if (value == null
                    || value.isBlank()) {
                continue;
            }

            if (!query.isEmpty()) {
                query.append("&");
            }

            query.append(
                    URLEncoder.encode(
                            fieldName,
                            StandardCharsets.UTF_8
                    )
            );

            query.append("=");

            query.append(
                    URLEncoder.encode(
                            value,
                            StandardCharsets.UTF_8
                    )
            );
        }

        return query.toString();
    }

    private String hmacSHA512(
            String secretKey,
            String data
    ) {
        try {
            Mac hmac =
                    Mac.getInstance("HmacSHA512");

            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(
                            secretKey.getBytes(
                                    StandardCharsets.UTF_8
                            ),
                            "HmacSHA512"
                    );

            hmac.init(secretKeySpec);

            byte[] hash =
                    hmac.doFinal(
                            data.getBytes(
                                    StandardCharsets.UTF_8
                            )
                    );

            StringBuilder result =
                    new StringBuilder();

            for (byte b : hash) {
                result.append(
                        String.format(
                                "%02x",
                                b
                        )
                );
            }

            return result.toString();

        } catch (Exception e) {
            return null;
        }
    }

    private String getCurrentDate() {
        return formatDate(
                new Date()
        );
    }

    private String getExpireDate() {
        long expireTime =
                System.currentTimeMillis()
                        + 15 * 60 * 1000L;

        return formatDate(
                new Date(expireTime)
        );
    }

    private String formatDate(Date date) {
        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "yyyyMMddHHmmss"
                );

        formatter.setTimeZone(
                TimeZone.getTimeZone(
                        "Asia/Ho_Chi_Minh"
                )
        );

        return formatter.format(date);
    }

    private String getClientIpAddress(
            HttpServletRequest request
    ) {
        String ip =
                request.getHeader(
                        "X-Forwarded-For"
                );

        if (ip != null
                && !ip.isBlank()) {
            return ip.split(",")[0].trim();
        }

        ip = request.getHeader(
                "Proxy-Client-IP"
        );

        if (ip != null
                && !ip.isBlank()) {
            return ip;
        }

        ip = request.getHeader(
                "WL-Proxy-Client-IP"
        );

        if (ip != null
                && !ip.isBlank()) {
            return ip;
        }

        return request.getRemoteAddr();
    }
}
