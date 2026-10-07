package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    Page<Product> findByStatusTrue(Pageable pageable);

    Page<Product> findByCategoryIdAndStatusTrue(
            Long categoryId,
            Pageable pageable
    );

    @Query("SELECT p FROM Product p WHERE p.category.id = :categoryId AND p.status = true "
            + "AND (:minPrice IS NULL OR p.price >= :minPrice) "
            + "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Product> findActiveByCategoryAndPriceRange(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
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

    List<Product> findTop10ByStatusTrueOrderByCreatedAtDesc();

    List<Product> findTop10ByStatusTrueOrderByPriceAsc();

    List<Product> findTop10ByStatusTrueOrderByPriceDesc();

    long countByStatusTrue();
}
