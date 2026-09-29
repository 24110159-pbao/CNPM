package com.example.ecommerce.controller.manager;

import com.example.ecommerce.service.DiscountService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/manager/discounts")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerDiscountController {

    private final DiscountService discountService;

    @GetMapping
    public String list(
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        model.addAttribute(
                "discounts",
                discountService.getAllDiscountCodes(pageable)
        );

        return "manager/discounts/list";
    }

    @GetMapping("/create")
    public String createForm() {
        return "manager/discounts/form";
    }

    @PostMapping("/create")
    public String create(
            @RequestParam String code,
            @RequestParam Integer discountValue,
            @RequestParam Integer quantity,
            @RequestParam LocalDateTime startAt,
            @RequestParam LocalDateTime endAt,
            RedirectAttributes redirectAttributes) {

        discountService.createDiscount(
                code,
                discountValue,
                quantity,
                startAt,
                endAt
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Tạo mã giảm giá thành công."
        );

        return "redirect:/manager/discounts";
    }

    @GetMapping("/{id}/edit")
    public String editForm(
            @PathVariable Long id,
            Model model) {

        model.addAttribute(
                "discount",
                discountService.findById(id)
        );

        return "manager/discounts/form";
    }

    @PostMapping("/{id}/edit")
    public String edit(
            @PathVariable Long id,
            @RequestParam String code,
            @RequestParam Integer discountValue,
            @RequestParam Integer quantity,
            @RequestParam LocalDateTime startAt,
            @RequestParam LocalDateTime endAt,
            RedirectAttributes redirectAttributes) {

        discountService.updateDiscount(
                id,
                code,
                discountValue,
                quantity,
                startAt,
                endAt
        );

        redirectAttributes.addFlashAttribute(
                "success",
                "Cập nhật mã giảm giá thành công."
        );

        return "redirect:/manager/discounts";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        discountService.deleteDiscount(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã xóa mã giảm giá."
        );

        return "redirect:/manager/discounts";
    }
}
