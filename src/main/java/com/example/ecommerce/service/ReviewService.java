package com.example.ecommerce.service;

import com.example.ecommerce.entity.Review;
import com.example.ecommerce.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    /**
     * Lấy Review của một sản phẩm.
     *
     * Guest được phép xem Review.
     */
    public Page<Review> getReviewsByProduct(
            Long productId,
            Pageable pageable
    ) {
        return reviewRepository
                .findByProductIdOrderByCreatedAtDesc(
                        productId,
                        pageable
                );
    }
}
