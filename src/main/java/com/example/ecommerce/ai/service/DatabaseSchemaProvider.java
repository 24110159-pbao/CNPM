package com.example.ecommerce.ai.service;

import org.springframework.stereotype.Component;

/** Safe, curated SQL schema context based on the project's JPA entities. */
@Component
public class DatabaseSchemaProvider {

	public String getSchemaAndRules() {
		return """
				MySQL schema (physical table/column names use Spring's standard snake_case naming):
				- users(id, name, email, phone, address, role, created_at, updated_at). The password column exists but MUST NEVER be selected.
				- categories(id, name)
				- products(id, category_id, name, description, price, stock, image_url, status, created_at)
				- product_specs(id, product_id, ram, storage, color, screen_size, battery)
				- carts(id, user_id)
				- cart_items(id, cart_id, product_id, quantity)
				- orders(id, user_id, recipient_name, recipient_phone, shipping_address, subtotal, discount_amount, discount_code, final_amount, status, created_at, updated_at)
				- order_items(id, order_id, product_id, quantity, price). price is the purchase-time snapshot.
				- payments(id, order_id, method, status, transaction_no, paid_at, created_at)
				- discount_codes(id, code, discount_value, quantity, start_at, end_at). discount_value is a percentage.
				- reviews(id, user_id, product_id, order_id, rating, comment, created_at)
				- notifications(id, user_id, title, message, type, is_read, created_at)
				- otp_verifications exists but contains OTP credentials: do not query it or return OTP-related data.
				Relationships: products.category_id -> categories.id; product_specs.product_id -> products.id;
				carts.user_id -> users.id; cart_items.cart_id -> carts.id and cart_items.product_id -> products.id;
				orders.user_id -> users.id; order_items.order_id -> orders.id and order_items.product_id -> products.id;
				payments.order_id -> orders.id; reviews.user_id -> users.id, reviews.product_id -> products.id, reviews.order_id -> orders.id;
				notifications.user_id -> users.id.
				Enums: users.role is USER or MANAGER; orders.status is PENDING, CONFIRMED, SHIPPING, DELIVERED, CANCELLED, RETURN_REQUESTED, REFUNDED;
				payments.method is COD or VNPAY; payments.status is PENDING, PAID, FAILED, REFUNDED.
				Business rules: order_items.price is the historic unit-price snapshot; orders store recipient/shipping snapshots.
				For actual revenue, count only orders.status = 'DELIVERED' unless the manager explicitly requests another scope.
				Use orders.final_amount for order revenue; do not infer a discount usage-history table.
				Review eligibility is tied to delivered purchases. There are no product_images, user_addresses, or discount_usages tables.
				""";
	}
}
