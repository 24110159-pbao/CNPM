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
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        Page<?> products =
                productService.getProductsByCategory(id, pageable);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("category", categoryService.findById(id));

        return "product/list";
    }
}
