package com.example.ecommerce.controller;

import com.example.ecommerce.service.CategoryService;
import com.example.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping
    public String categories(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        return "category/list";
    }

    @GetMapping("/{id}")
    public String productsByCategory(
            @PathVariable Long id,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        boolean invalidPriceRange = (minPrice != null && minPrice.signum() < 0)
            || (maxPrice != null && maxPrice.signum() < 0)
            || (minPrice != null && maxPrice != null
            && minPrice.compareTo(maxPrice) > 0);

        Page<?> products = invalidPriceRange
            ? productService.getProductsByCategory(id, pageable)
            : productService.getProductsByCategoryAndPriceRange(
                id,
                minPrice,
                maxPrice,
                pageable
            );

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", categoryService.findById(id));
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        if (invalidPriceRange) {
            model.addAttribute("priceFilterError", "Khoảng giá không hợp lệ.");
        }

        return "product/list";
    }
}
