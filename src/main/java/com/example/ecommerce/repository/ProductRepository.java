package com.example.ecommerce.repository;

import com.example.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
