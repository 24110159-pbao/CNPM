package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

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

    List<Product> findTop10ByStatusTrueOrderByCreatedAtDesc();

    List<Product> findTop10ByStatusTrueOrderByPriceAsc();

    List<Product> findTop10ByStatusTrueOrderByPriceDesc();

    long countByStatusTrue();
}
