package com.example.ecommerce.controller.manager;

import com.example.ecommerce.service.CategoryService;
import com.example.ecommerce.service.CloudinaryService;
import com.example.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/products")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CloudinaryService cloudinaryService;

    @GetMapping
    public String list(
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        model.addAttribute(
                "products",
                productService.getAllProducts(pageable)
        );

        return "manager/products/list";
    }

    @GetMapping("/create")
    public String createForm(Model model) {

        model.addAttribute(
                "categories",
                categoryService.getAllCategories()
        );

        return "manager/products/form";
    }

    @PostMapping("/create")
    public String create(
            @RequestParam Long categoryId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam java.math.BigDecimal price,
            @RequestParam Integer stock,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String ram,
            @RequestParam(required = false) String storage,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String screenSize,
            @RequestParam(required = false) String battery,

            RedirectAttributes redirectAttributes) {

        productService.createProduct(
                name,
                description,
                price,
                stock,
                categoryId,
                image,
                ram,
                storage,
                color,
                screenSize,
                battery
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Thêm sản phẩm thành công."
        );

        return "redirect:/manager/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "product",
                productService.findById(id)
        );

        model.addAttribute(
                "categories",
                categoryService.getAllCategories()
        );

        return "manager/products/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @RequestParam Long categoryId,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam java.math.BigDecimal price,
            @RequestParam Integer stock,
            @RequestParam(required = false) MultipartFile image,
            @RequestParam(required = false) String ram,
            @RequestParam(required = false) String storage,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) String screenSize,
            @RequestParam(required = false) String battery,
            RedirectAttributes redirectAttributes) {

        productService.updateProduct(
                id,
                name,
                description,
                price,
                stock,
                categoryId,
                image,
                ram,
                storage,
                color,
                screenSize,
                battery
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Cập nhật sản phẩm thành công."
        );

        return "redirect:/manager/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        productService.deleteProduct(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã xóa sản phẩm."
        );

        return "redirect:/manager/products";
    }

    @PostMapping("/{id}/toggle-status")
    public String toggleStatus(
            @PathVariable Long id,
            @RequestParam boolean status,
            RedirectAttributes redirectAttributes) {

        productService.updateStatus(id, status);

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã cập nhật trạng thái sản phẩm."
        );

        return "redirect:/manager/products";
    }
}
