package com.example.ecommerce.service;

import com.example.ecommerce.entity.Category;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.CategoryRepository;
import com.example.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final CloudinaryService cloudinaryService;

    public Page<Product> getActiveProducts(Pageable pageable) {
        return productRepository.findByStatusTrue(pageable);
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product findById(Long id) {
        return productRepository.findById(id).orElse(null);
    }

    public Page<Product> getProductsByCategory(
            Long categoryId,
            Pageable pageable
    ) {
        return productRepository.findByCategoryIdAndStatusTrue(
                categoryId,
                pageable
        );
    }

    public Page<Product> searchProducts(
            String keyword,
            Pageable pageable
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return productRepository.findByStatusTrue(pageable);
        }

        return productRepository
                .findByNameContainingIgnoreCaseAndStatusTrue(
                        keyword.trim(),
                        pageable
                );
    }

    public Page<Product> searchProductsByCategory(
            String keyword,
            Long categoryId,
            Pageable pageable
    ) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getProductsByCategory(categoryId, pageable);
        }

        return productRepository
                .findByNameContainingIgnoreCaseAndCategoryIdAndStatusTrue(
                        keyword.trim(),
                        categoryId,
                        pageable
                );
    }

    public List<Product> getNewProducts() {
        return productRepository.findTop10ByStatusTrueOrderByCreatedAtDesc();
    }

    public List<Product> getCheapProducts() {
        return productRepository.findTop10ByStatusTrueOrderByPriceAsc();
    }

    public List<Product> getExpensiveProducts() {
        return productRepository.findTop10ByStatusTrueOrderByPriceDesc();
    }

    @Transactional
    public Product createProduct(
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            Long categoryId,
            MultipartFile image
    ) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            return null;
        }

        if (stock == null || stock < 0) {
            return null;
        }

        if (categoryId == null) {
            return null;
        }

        Category category = categoryRepository
                .findById(categoryId)
                .orElse(null);

        if (category == null) {
            return null;
        }

        String imageUrl = null;

        if (image != null && !image.isEmpty()) {
            try {
                imageUrl = cloudinaryService.uploadImage(image);
            } catch (IOException e) {
                return null;
            }
        }

        Product product = Product.builder()
                .name(name.trim())
                .description(description)
                .price(price)
                .stock(stock)
                .category(category)
                .imageUrl(imageUrl)
                .status(true)
                .build();

        return productRepository.save(product);
    }

    @Transactional
    public boolean updateProduct(
            Long productId,
            String name,
            String description,
            BigDecimal price,
            Integer stock,
            Long categoryId,
            MultipartFile image
    ) {
        Product product = productRepository
                .findById(productId)
                .orElse(null);

        if (product == null) {
            return false;
        }

        if (name == null || name.trim().isEmpty()) {
            return false;
        }

        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        if (stock == null || stock < 0) {
            return false;
        }

        Category category = categoryRepository
                .findById(categoryId)
                .orElse(null);

        if (category == null) {
            return false;
        }

        product.setName(name.trim());
        product.setDescription(description);
        product.setPrice(price);
        product.setStock(stock);
        product.setCategory(category);

        if (image != null && !image.isEmpty()) {
            try {
                String imageUrl = cloudinaryService.uploadImage(image);
                product.setImageUrl(imageUrl);
            } catch (IOException e) {
                return false;
            }
        }

        productRepository.save(product);

        return true;
    }

    @Transactional
    public boolean updateStock(
            Long productId,
            Integer stock
    ) {
        if (stock == null || stock < 0) {
            return false;
        }

        Product product = productRepository
                .findById(productId)
                .orElse(null);

        if (product == null) {
            return false;
        }

        product.setStock(stock);

        productRepository.save(product);

        return true;
    }

    @Transactional
    public boolean updatePrice(
            Long productId,
            BigDecimal price
    ) {
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            return false;
        }

        Product product = productRepository
                .findById(productId)
                .orElse(null);

        if (product == null) {
            return false;
        }

        product.setPrice(price);

        productRepository.save(product);

        return true;
    }

    @Transactional
    public boolean updateStatus(
            Long productId,
            boolean status
    ) {
        Product product = productRepository
                .findById(productId)
                .orElse(null);

        if (product == null) {
            return false;
        }

        product.setStatus(status);

        productRepository.save(product);

        return true;
    }

    @Transactional
    public boolean deleteProduct(Long productId) {
        Product product = productRepository
                .findById(productId)
                .orElse(null);

        if (product == null) {
            return false;
        }

        /*
         * Product được yêu cầu xóa thật khỏi database.
         *
         * Tuy nhiên Product có thể đang được tham chiếu bởi:
         * - CartItem
         * - OrderItem
         * - Review
         *
         * Nếu database có FK, delete có thể thất bại.
         *
         * Không tự ý cascade delete các dữ liệu lịch sử.
         */
        try {
            productRepository.delete(product);
            productRepository.flush();
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
