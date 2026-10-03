package com.example.ecommerce.ai.service;

import org.springframework.stereotype.Component;

/**
 * Cung cấp schema và business rules cho AI.
 *
 * Chỉ mô tả những bảng/column thực tế được phép sử dụng.
 */
@Component
public class DatabaseSchemaProvider {

	public String getSchemaAndRules() {

		return """
                DATABASE: ecommerce

                MySQL physical schema.
                Table and column names use snake_case.

                ==================================================
                USERS
                ==================================================

                users(
                    id,
                    name,
                    email,
                    phone,
                    address,
                    role,
                    password,
                    created_at,
                    updated_at
                )

                Rules:
                - email is UNIQUE.
                - role is USER or MANAGER.
                - password exists but MUST NEVER be selected.
                - Never query password.
                - Never expose password hash.

                ==================================================
                CATEGORIES
                ==================================================

                categories(
                    id,
                    name
                )

                ==================================================
                PRODUCTS
                ==================================================

                products(
                    id,
                    category_id,
                    name,
                    description,
                    price,
                    stock,
                    image_url,
                    status,
                    created_at
                )

                Rules:
                - category_id references categories.id.
                - status is BOOLEAN.
                - There is only one product image URL.
                - image_url may be ignored unless explicitly requested.

                ==================================================
                PRODUCT SPECS
                ==================================================

                product_specs(
                    id,
                    product_id,
                    ram,
                    storage,
                    color,
                    screen_size,
                    battery
                )

                Rules:
                - product_id references products.id.

                ==================================================
                CARTS
                ==================================================

                carts(
                    id,
                    user_id
                )

                Rules:
                - One user has one cart.

                ==================================================
                CART ITEMS
                ==================================================

                cart_items(
                    id,
                    cart_id,
                    product_id,
                    quantity
                )

                Rules:
                - cart_id references carts.id.
                - product_id references products.id.

                ==================================================
                ORDERS
                ==================================================

                orders(
                    id,
                    user_id,
                    recipient_name,
                    recipient_phone,
                    shipping_address,
                    subtotal,
                    discount_amount,
                    discount_code,
                    final_amount,
                    status,
                    created_at,
                    updated_at
                )

                Rules:
                - user_id references users.id.
                - recipient_name is shipping snapshot.
                - recipient_phone is shipping snapshot.
                - shipping_address is shipping snapshot.
                - final_amount is the final amount after discount.
                - status is one of:
                  PENDING,
                  CONFIRMED,
                  SHIPPING,
                  DELIVERED,
                  CANCELLED,
                  RETURN_REQUESTED,
                  REFUNDED.

                ==================================================
                ORDER ITEMS
                ==================================================

                order_items(
                    id,
                    order_id,
                    product_id,
                    quantity,
                    price
                )

                Rules:
                - order_id references orders.id.
                - product_id references products.id.
                - price is the purchase-time price snapshot.
                - quantity is purchased quantity.

                ==================================================
                PAYMENTS
                ==================================================

                payments(
                    id,
                    order_id,
                    method,
                    status,
                    transaction_no,
                    paid_at,
                    created_at
                )

                Rules:
                - order_id references orders.id.
                - method is COD or VNPAY.
                - status is:
                  PENDING,
                  PAID,
                  FAILED,
                  REFUNDED.

                ==================================================
                DISCOUNT CODES
                ==================================================

                discount_codes(
                    id,
                    code,
                    discount_value,
                    quantity,
                    start_at,
                    end_at
                )

                Rules:
                - discount_value is percentage.
                - quantity represents remaining uses.
                - One order can use at most one discount code.
                - There is no discount_usages table.

                ==================================================
                REVIEWS
                ==================================================

                reviews(
                    id,
                    user_id,
                    product_id,
                    order_id,
                    rating,
                    comment,
                    created_at
                )

                Rules:
                - user_id references users.id.
                - product_id references products.id.
                - order_id references orders.id.
                - rating is between 1 and 5.
                - Unique(user_id, product_id, order_id).
                - Review is valid only for delivered purchases.

                ==================================================
                NOTIFICATIONS
                ==================================================

                notifications(
                    id,
                    user_id,
                    title,
                    message,
                    type,
                    is_read,
                    created_at
                )

                ==================================================
                OTP
                ==================================================

                otp_verifications exists.

                IMPORTANT:
                - Never query otp_verifications.
                - Never return OTP.
                - Never return OTP verification data.

                ==================================================
                RELATIONSHIPS
                ==================================================

                products.category_id -> categories.id

                product_specs.product_id -> products.id

                carts.user_id -> users.id

                cart_items.cart_id -> carts.id

                cart_items.product_id -> products.id

                orders.user_id -> users.id

                order_items.order_id -> orders.id

                order_items.product_id -> products.id

                payments.order_id -> orders.id

                reviews.user_id -> users.id

                reviews.product_id -> products.id

                reviews.order_id -> orders.id

                notifications.user_id -> users.id

                ==================================================
                BUSINESS RULES
                ==================================================

                1. Only SELECT statements are allowed.

                2. Never select:
                   - password
                   - OTP
                   - API key
                   - secret
                   - token
                   - credentials

                3. Never use SELECT *.

                4. Actual revenue normally means:
                   orders.status = 'DELIVERED'

                5. For revenue:
                   use orders.final_amount.

                6. order_items.price is the historic unit price.

                7. Do not infer a discount usage-history table.

                8. There are no:
                   - user_addresses
                   - product_images
                   - discount_usages

                9. Product status is BOOLEAN.

                10. Discount is percentage based.

                11. One order can use at most one discount.

                12. For questions involving dates, use the current date
                    supplied by the application.

                13. Never invent tables or columns.

                14. If the schema does not contain enough information,
                    do not guess.

                15. Only query the approved ecommerce tables.
                """;
	}
}
