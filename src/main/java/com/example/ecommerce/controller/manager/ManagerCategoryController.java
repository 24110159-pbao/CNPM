package com.example.ecommerce.controller.manager;

import com.example.ecommerce.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/categories")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerCategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public String list(Model model) {

        model.addAttribute(
                "categories",
                categoryService.getAllCategories()
        );

        return "manager/categories/list";
    }

    @PostMapping("/create")
    public String create(
            @RequestParam String name,
            RedirectAttributes redirectAttributes) {

        categoryService.createCategory(name);

        redirectAttributes.addFlashAttribute(
                "success",
                "Thêm danh mục thành công."
        );

        return "redirect:/manager/categories";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @RequestParam String name,
            RedirectAttributes redirectAttributes) {

        categoryService.updateCategory(id, name);

        redirectAttributes.addFlashAttribute(
                "success",
                "Cập nhật danh mục thành công."
        );

        return "redirect:/manager/categories";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        categoryService.deleteCategory(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã xóa danh mục."
        );

        return "redirect:/manager/categories";
    }
}
