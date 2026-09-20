package com.example.ecommerce.controller;

import com.example.ecommerce.service.CategoryService;
import com.example.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {

        // Lấy danh mục
        model.addAttribute(
                "categories",
                categoryService.getAllCategories()
        );

        // Lấy sản phẩm mới
        model.addAttribute(
                "newestProducts",
                productService.getNewestProducts(
                        PageRequest.of(0, 8)
                ).getContent()
        );

        return "home";
    }
}
