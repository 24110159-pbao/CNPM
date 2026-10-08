package com.example.ecommerce.service;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAllCategories() {
        return categoryRepository.findAllByOrderByIdDesc();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id).orElse(null);
    }

    public Category findByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        return categoryRepository
                .findByNameIgnoreCase(name.trim())
                .orElse(null);
    }

    @Transactional
    public Category createCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        String normalizedName = name.trim();

        if (categoryRepository.existsByNameIgnoreCase(normalizedName)) {
            return null;
        }

        Category category = Category.builder()
                .name(normalizedName)
                .build();

        return categoryRepository.save(category);
    }

    @Transactional
    public boolean updateCategory(
            Long categoryId,
            String name
    ) {
        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        Category category = categoryRepository
                .findById(categoryId)
                .orElse(null);

        if (category == null) {
            return false;
        }

        String normalizedName = name.trim();

        Category existingCategory = categoryRepository
                .findByNameIgnoreCase(normalizedName)
                .orElse(null);

        if (existingCategory != null
                && !existingCategory.getId().equals(categoryId)) {
            return false;
        }

        category.setName(normalizedName);

        categoryRepository.save(category);

        return true;
    }

    @Transactional
    public boolean deleteCategory(Long categoryId) {
        Category category = categoryRepository
                .findById(categoryId)
                .orElse(null);

        if (category == null) {
            return false;
        }

        /*
         * Product đang tham chiếu Category.
         * Không cascade delete Product khi xóa Category.
         */
        try {
            categoryRepository.delete(category);
            categoryRepository.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
