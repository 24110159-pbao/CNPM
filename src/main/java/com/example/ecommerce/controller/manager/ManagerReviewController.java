package com.example.ecommerce.controller.manager;

import com.example.ecommerce.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/reviews")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerReviewController {

    private final ReviewService reviewService;

    @GetMapping
    public String list(
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        model.addAttribute(
                "reviews",
                reviewService.getAllReviews(pageable)
        );

        return "manager/reviews/list";
    }

    @PostMapping("/{id}/delete")
    public String delete(
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {

        reviewService.deleteReview(id);

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã xóa review."
        );

        return "redirect:/manager/reviews";
    }
}
