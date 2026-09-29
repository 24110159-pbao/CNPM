package com.example.ecommerce.controller.manager;

import com.example.ecommerce.enums.Role;
import com.example.ecommerce.service.OrderService;
import com.example.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/manager/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('MANAGER')")
public class ManagerUserController {

    private final UserService userService;
    private final OrderService orderService;

    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        if (keyword == null || keyword.isBlank()) {
            model.addAttribute(
                    "users",
                    userService.getAllUsers(pageable)
            );
        } else {
            model.addAttribute(
                    "users",
                    userService.searchUsers(
                            keyword,
                            pageable
                    )
            );
        }

        model.addAttribute("keyword", keyword);

        return "manager/users/list";
    }

    @GetMapping("/{id}")
    public String detail(
            @PathVariable Long id,
            @PageableDefault(size = 20) Pageable pageable,
            Model model) {

        model.addAttribute(
                "user",
                userService.findById(id)
        );

        model.addAttribute(
                "orders",
                orderService.getUserOrders(
                        id,
                        pageable
                )
        );

        return "manager/users/detail";
    }

    @PostMapping("/{id}/role")
    public String updateRole(
            @PathVariable Long id,
            @RequestParam Role role,
            RedirectAttributes redirectAttributes) {

        userService.updateRole(id, role);

        redirectAttributes.addFlashAttribute(
                "success",
                "Đã cập nhật quyền người dùng."
        );

        return "redirect:/manager/users/" + id;
    }
}
