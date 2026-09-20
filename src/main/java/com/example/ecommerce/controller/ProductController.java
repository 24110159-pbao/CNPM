package com.example.ecommerce.controller;

import com.example.ecommerce.service.CategoryService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;

    private static final int PAGE_SIZE = 20;

    /**
     * Danh sách sản phẩm.
     *
     * Hỗ trợ:
     * - Phân trang
     * - Tìm kiếm theo tên
     * - Lọc theo category
     * - Lọc theo giá
     * - Tìm kiếm + category
     */
    @GetMapping("/products")
    public String products(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {

        // Không cho page âm
        if (page < 0) {
            page = 0;
        }

        Pageable pageable = PageRequest.of(page, PAGE_SIZE);

        Page<com.example.ecommerce.entity.Product> productPage;

        /*
         * Trường hợp có cả keyword + category.
         */
        if (hasText(keyword) && categoryId != null) {

            productPage = productService.searchProductsByCategory(
                    keyword.trim(),
                    categoryId,
                    pageable
            );

            /*
             * Chỉ tìm kiếm.
             */
        } else if (hasText(keyword)) {

            productPage = productService.searchProducts(
                    keyword.trim(),
                    pageable
            );

            /*
             * Chỉ category.
             */
        } else if (categoryId != null) {

            productPage = productService.getProductsByCategory(
                    categoryId,
                    pageable
            );

            /*
             * Có filter giá.
             */
        } else if (minPrice != null || maxPrice != null) {

            productPage = productService.getProductsByPrice(
                    minPrice,
                    maxPrice,
                    pageable
            );

            /*
             * Không có filter.
             */
        } else {

            productPage = productService.getActiveProducts(
                    pageable
            );
        }

        // Products
        model.addAttribute("page", productPage);

        // Categories cho sidebar
        model.addAttribute(
                "categories",
                categoryService.getAllCategories()
        );

        // Giữ lại giá trị filter trên giao diện
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        return "product/products";
    }

    /**
     * Chi tiết sản phẩm.
     */
    @GetMapping("/products/{id}")
    public String productDetail(
            @PathVariable Long id,
            @RequestParam(
                    name = "reviewPage",
                    defaultValue = "0"
            ) int reviewPage,
            Model model
    ) {

        if (reviewPage < 0) {
            reviewPage = 0;
        }

        // Product
        model.addAttribute(
                "product",
                productService.getActiveProductById(id)
        );

        // Reviews
        Pageable pageable = PageRequest.of(
                reviewPage,
                5
        );

        model.addAttribute(
                "reviews",
                reviewService.getReviewsByProduct(
                        id,
                        pageable
                )
        );

        return "product/product-detail";
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
