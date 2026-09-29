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

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public Cart getOrCreateCart(Long userId) {
        Cart existingCart = cartRepository
                .findByUserId(userId)
                .orElse(null);

        if (existingCart != null) {
            return existingCart;
        }

        User user = userRepository
                .findById(userId)
                .orElse(null);

        if (user == null) {
            return null;
        }

        Cart cart = Cart.builder()
                .user(user)
                .build();

        return cartRepository.save(cart);
    }

    public Cart getCart(Long userId) {
        return cartRepository
                .findByUserId(userId)
                .orElse(null);
    }

    @Transactional
    public boolean addToCart(
            Long userId,
            Long productId,
            Integer quantity
    ) {
        if (quantity == null || quantity <= 0) {
            return false;
        }

        Cart cart = getOrCreateCart(userId);

        if (cart == null) {
            return false;
        }

        Product product = productRepository
                .findById(productId)
                .orElse(null);

        if (product == null || !Boolean.TRUE.equals(product.getStatus())) {
            return false;
        }

        if (product.getStock() < quantity) {
            return false;
        }

        CartItem cartItem = cartItemRepository
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
                return false;
            }

            cartItem.setQuantity(newQuantity);
        }

        cartItemRepository.save(cartItem);

        return true;
    }

    @Transactional
    public boolean increaseQuantity(
            Long userId,
            Long productId
    ) {
        Cart cart = getCart(userId);

        if (cart == null) {
            return false;
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        productId
                )
                .orElse(null);

        if (cartItem == null) {
            return false;
        }

        Product product = cartItem.getProduct();

        int newQuantity = cartItem.getQuantity() + 1;

        if (newQuantity > product.getStock()) {
            return false;
        }

        cartItem.setQuantity(newQuantity);

        cartItemRepository.save(cartItem);

        return true;
    }

    @Transactional
    public boolean decreaseQuantity(
            Long userId,
            Long productId
    ) {
        Cart cart = getCart(userId);

        if (cart == null) {
            return false;
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        productId
                )
                .orElse(null);

        if (cartItem == null) {
            return false;
        }

        if (cartItem.getQuantity() <= 1) {
            cartItemRepository.delete(cartItem);
            return true;
        }

        cartItem.setQuantity(
                cartItem.getQuantity() - 1
        );

        cartItemRepository.save(cartItem);

        return true;
    }

    @Transactional
    public boolean removeFromCart(
            Long userId,
            Long productId
    ) {
        Cart cart = getCart(userId);

        if (cart == null) {
            return false;
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        productId
                )
                .orElse(null);

        if (cartItem == null) {
            return false;
        }

        cartItemRepository.delete(cartItem);

        return true;
    }

    @Transactional
    public boolean updateQuantity(
            Long userId,
            Long productId,
            Integer quantity
    ) {
        if (quantity == null || quantity <= 0) {
            return false;
        }

        Cart cart = getCart(userId);

        if (cart == null) {
            return false;
        }

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getId(),
                        productId
                )
                .orElse(null);

        if (cartItem == null) {
            return false;
        }

        Product product = cartItem.getProduct();

        if (quantity > product.getStock()) {
            return false;
        }

        cartItem.setQuantity(quantity);

        cartItemRepository.save(cartItem);

        return true;
    }

    @Transactional
    public boolean clearCart(Long userId) {
        Cart cart = getCart(userId);

        if (cart == null) {
            return false;
        }

        cartItemRepository.deleteByCartId(cart.getId());

        return true;
    }
}
