package com.example.ecommerce.controller;

import com.example.ecommerce.service.CategoryService;
import com.example.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping
@RequiredArgsConstructor
public class HomeController {

    private final CategoryService categoryService;
    private final ProductService productService;

    @GetMapping({"/", "/home"})
    public String home(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("newProducts", productService.getNewProducts());
        model.addAttribute("cheapProducts", productService.getCheapProducts());

        return "home/index";
    }
}
