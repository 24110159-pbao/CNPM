package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStatusTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndStatusTrue(
            Long categoryId,
            Pageable pageable
    );

    Page<Product> findByNameContainingIgnoreCaseAndStatusTrue(
            String name,
            Pageable pageable
    );

    Page<Product> findByNameContainingIgnoreCaseAndCategoryIdAndStatusTrue(
            String name,
            Long categoryId,
            Pageable pageable
    );

    Page<Product> findByCategoryId(
            Long categoryId,
            Pageable pageable
    );

    Page<Product> findByNameContainingIgnoreCase(
            String name,
            Pageable pageable
    );

    /*
     * Lọc sản phẩm đang hiển thị theo khoảng giá.
     *
     * minPrice và maxPrice có thể null.
     */
    Page<Product> findByStatusTrueAndPriceBetween(
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    );

    Page<Product> findByStatusTrueAndPriceGreaterThanEqual(
            BigDecimal minPrice,
            Pageable pageable
    );

    Page<Product> findByStatusTrueAndPriceLessThanEqual(
            BigDecimal maxPrice,
            Pageable pageable
    );
}
