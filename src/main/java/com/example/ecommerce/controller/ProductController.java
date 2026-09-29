package com.example.ecommerce.controller;

import com.example.ecommerce.service.CategoryService;
import com.example.ecommerce.service.ProductService;
import com.example.ecommerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;

    @GetMapping
    public String listProducts(
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        Page<?> products = productService.getActiveProducts(pageable);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "product/list";
    }

    @GetMapping("/{id}")
    public String productDetail(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        model.addAttribute("product", productService.findById(id));
        model.addAttribute(
                "reviews",
                reviewService.getProductReviews(id, pageable)
        );

        return "product/detail";
    }

    @GetMapping("/search")
    public String searchProducts(
            @RequestParam(required = false, defaultValue = "") String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        Page<?> products = productService.searchProducts(keyword, pageable);

        model.addAttribute("products", products);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "product/list";
    }
}
