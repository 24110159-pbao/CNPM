package com.example.ecommerce.service;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.entity.User;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;
import com.example.ecommerce.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /*
     * ============================
     * GET CART
     * ============================
     */

    /*
     * Lấy Cart của User.
     *
     * Nếu User chưa có Cart thì tạo Cart mới.
     */
    @Transactional
    public Cart getOrCreateCart(Long userId) {

        return cartRepository.findByUserId(userId)
                .orElseGet(() -> {

                    User user = userRepository.findById(userId)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy User"
                                    )
                            );

                    Cart cart = Cart.builder()
                            .user(user)
                            .items(new ArrayList<>())
                            .build();

                    return cartRepository.save(cart);
                });
    }

    /*
     * ============================
     * ADD PRODUCT
     * ============================
     */

    /*
     * Thêm Product vào Cart.
     *
     * Nếu Product đã có:
     * quantity mới = quantity cũ + quantity thêm vào
     */
    @Transactional
    public CartItem addProduct(
            Long userId,
            Long productId,
            int quantity
    ) {

        if (quantity <= 0) {
            throw new RuntimeException(
                    "Số lượng phải lớn hơn 0"
            );
        }

        Cart cart = getOrCreateCart(userId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy Product"
                        )
                );

        if (!Boolean.TRUE.equals(product.getStatus())) {
            throw new RuntimeException(
                    "Product hiện không được bán"
            );
        }

        if (product.getStock() < quantity) {
            throw new RuntimeException(
                    "Không đủ tồn kho"
            );
        }

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                productId
                        )
                        .orElse(null);

        if (cartItem == null) {

            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .build();

        } else {

            int newQuantity =
                    cartItem.getQuantity() + quantity;

            if (newQuantity > product.getStock()) {
                throw new RuntimeException(
                        "Số lượng vượt quá tồn kho"
                );
            }

            cartItem.setQuantity(newQuantity);
        }

        return cartItemRepository.save(cartItem);
    }

    /*
     * ============================
     * UPDATE QUANTITY
     * ============================
     */

    /*
     * Thay đổi quantity của CartItem.
     */
    @Transactional
    public CartItem updateQuantity(
            Long userId,
            Long productId,
            int quantity
    ) {

        if (quantity <= 0) {
            throw new RuntimeException(
                    "Số lượng phải lớn hơn 0"
            );
        }

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy Cart"
                        )
                );

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                productId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product không có trong Cart"
                                )
                        );

        Product product = cartItem.getProduct();

        if (!Boolean.TRUE.equals(product.getStatus())) {
            throw new RuntimeException(
                    "Product hiện không được bán"
            );
        }

        if (quantity > product.getStock()) {
            throw new RuntimeException(
                    "Số lượng vượt quá tồn kho"
            );
        }

        cartItem.setQuantity(quantity);

        return cartItemRepository.save(cartItem);
    }

    /*
     * ============================
     * INCREASE
     * ============================
     */

    /*
     * Tăng quantity lên 1.
     */
    @Transactional
    public CartItem increaseQuantity(
            Long userId,
            Long productId
    ) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy Cart"
                        )
                );

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                productId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product không có trong Cart"
                                )
                        );

        Product product = cartItem.getProduct();

        int newQuantity =
                cartItem.getQuantity() + 1;

        if (newQuantity > product.getStock()) {
            throw new RuntimeException(
                    "Không đủ tồn kho"
            );
        }

        cartItem.setQuantity(newQuantity);

        return cartItemRepository.save(cartItem);
    }

    /*
     * ============================
     * DECREASE
     * ============================
     */

    /*
     * Giảm quantity xuống 1.
     *
     * Nếu quantity = 1 thì xóa Product
     * khỏi Cart.
     */
    @Transactional
    public void decreaseQuantity(
            Long userId,
            Long productId
    ) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy Cart"
                        )
                );

        CartItem cartItem =
                cartItemRepository
                        .findByCartIdAndProductId(
                                cart.getId(),
                                productId
                        )
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Product không có trong Cart"
                                )
                        );

        if (cartItem.getQuantity() <= 1) {

            cartItemRepository.delete(cartItem);

        } else {

            cartItem.setQuantity(
                    cartItem.getQuantity() - 1
            );

            cartItemRepository.save(cartItem);
        }
    }

    /*
     * ============================
     * REMOVE
     * ============================
     */

    /*
     * Xóa Product khỏi Cart.
     */
    @Transactional
    public void removeProduct(
            Long userId,
            Long productId
    ) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy Cart"
                        )
                );

        if (!cartItemRepository.existsByCartIdAndProductId(
                cart.getId(),
                productId
        )) {
            throw new RuntimeException(
                    "Product không có trong Cart"
            );
        }

        cartItemRepository.deleteByCartIdAndProductId(
                cart.getId(),
                productId
        );
    }

    /*
     * ============================
     * CLEAR CART
     * ============================
     */

    /*
     * Xóa toàn bộ CartItem.
     *
     * Dùng sau khi tạo Order thành công.
     */
    @Transactional
    public void clearCart(Long userId) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Không tìm thấy Cart"
                        )
                );

        /*
         * Vì CartItem có orphanRemoval = true,
         * ta có thể xóa trực tiếp các item.
         */
        cart.getItems().clear();

        cartRepository.save(cart);
    }

    /*
     * ============================
     * CHECK CART
     * ============================
     */

    /*
     * Kiểm tra Cart có Product hay không.
     */
    public boolean containsProduct(
            Long userId,
            Long productId
    ) {

        Cart cart = cartRepository.findByUserId(userId)
                .orElse(null);

        if (cart == null) {
            return false;
        }

        return cartItemRepository.existsByCartIdAndProductId(
                cart.getId(),
                productId
        );
    }
}
