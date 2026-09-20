package com.example.ecommerce.service;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Lấy tất cả sản phẩm đang hiển thị.
     */
    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByStatusTrue(pageable);
    }

    /**
     * Tìm kiếm sản phẩm theo tên.
     */
    public Page<Product> searchProducts(
            String keyword,
            Pageable pageable
    ) {
        return productRepository
                .findByNameContainingIgnoreCaseAndStatusTrue(
                        keyword,
                        pageable
                );
    }

    /**
     * Lọc sản phẩm theo Category.
     */
    public Page<Product> getProductsByCategory(
            Long categoryId,
            Pageable pageable
    ) {
        return productRepository
                .findByCategoryIdAndStatusTrue(
                        categoryId,
                        pageable
                );
    }

    /**
     * Lấy sản phẩm theo Category + keyword.
     */
    public Page<Product> searchProductsByCategory(
            String keyword,
            Long categoryId,
            Pageable pageable
    ) {
        return productRepository
                .findByNameContainingIgnoreCaseAndCategoryIdAndStatusTrue(
                        keyword,
                        categoryId,
                        pageable
                );
    }

    /**
     * Lấy sản phẩm mới nhất.
     */
    public Page<Product> getNewestProducts(Pageable pageable) {
        return productRepository
                .findByStatusTrue(
                        pageable
                );
    }

    /**
     * Lấy sản phẩm theo ID.
     * Chỉ cho phép lấy sản phẩm đang hiển thị.
     */
    public Product getActiveProductById(Long id) {
        return productRepository
                .findById(id)
                .filter(Product::getStatus)
                .orElseThrow(() ->
                        new RuntimeException("Không tìm thấy sản phẩm")
                );
    }

    /**
     * Lọc sản phẩm theo khoảng giá.
     *
     * Phần này sẽ cần bổ sung method tương ứng
     * trong ProductRepository.
     */
    public Page<Product> getProductsByPrice(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        if (minPrice != null && maxPrice != null) {

            if (minPrice.compareTo(maxPrice) > 0) {
                throw new IllegalArgumentException(
                        "Giá tối thiểu không được lớn hơn giá tối đa"
                );
            }

            return productRepository.findByStatusTrueAndPriceBetween(
                    minPrice,
                    maxPrice,
                    pageable
            );
        }

        if (minPrice != null) {
            return productRepository.findByStatusTrueAndPriceGreaterThanEqual(
                    minPrice,
                    pageable
            );
        }

        if (maxPrice != null) {
            return productRepository.findByStatusTrueAndPriceLessThanEqual(
                    maxPrice,
                    pageable
            );
        }

        return productRepository.findByStatusTrue(pageable);
    }

}
