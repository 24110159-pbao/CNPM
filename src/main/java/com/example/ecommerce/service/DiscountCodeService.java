    package com.example.ecommerce.service;

    import com.example.ecommerce.entity.DiscountCode;
    import com.example.ecommerce.repository.DiscountCodeRepository;
    import lombok.RequiredArgsConstructor;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;

    import java.math.BigDecimal;
    import java.time.LocalDateTime;
    import java.util.Optional;

    @Service
    @RequiredArgsConstructor
    public class DiscountCodeService {

        private final DiscountCodeRepository discountCodeRepository;

        /*
         * ============================
         * FIND
         * ============================
         */

        public Optional<DiscountCode> findById(Long id) {
            return discountCodeRepository.findById(id);
        }

        public Optional<DiscountCode> findByCode(
                String code
        ) {

            if (code == null || code.trim().isEmpty()) {
                return Optional.empty();
            }

            return discountCodeRepository
                    .findByCodeIgnoreCase(
                            code.trim()
                    );
        }

        /*
         * Tìm code active.
         */
        public Optional<DiscountCode> findActiveByCode(
                String code
        ) {

            if (code == null || code.trim().isEmpty()) {
                return Optional.empty();
            }

            return discountCodeRepository
                    .findByCodeIgnoreCaseAndActiveTrue(
                            code.trim()
                    );
        }

        /*
         * ============================
         * VALIDATE
         * ============================
         */

        /*
         * Kiểm tra Discount Code có thể sử dụng
         * tại thời điểm hiện tại.
         */
        public boolean isValid(
                DiscountCode discountCode
        ) {

            if (discountCode == null) {
                return false;
            }

            if (!Boolean.TRUE.equals(
                    discountCode.getActive()
            )) {
                return false;
            }

            if (discountCode.getQuantity() == null
                    || discountCode.getQuantity() <= 0) {
                return false;
            }

            LocalDateTime now =
                    LocalDateTime.now();

            if (now.isBefore(
                    discountCode.getStartAt()
            )) {
                return false;
            }

            if (now.isAfter(
                    discountCode.getEndAt()
            )) {
                return false;
            }

            return true;
        }

        /*
         * Kiểm tra code bằng String.
         */
        public boolean isValidCode(
                String code
        ) {

            Optional<DiscountCode> optional =
                    findActiveByCode(code);

            return optional
                    .map(this::isValid)
                    .orElse(false);
        }

        /*
         * ============================
         * CALCULATE DISCOUNT
         * ============================
         */

        /*
         * Tính số tiền được giảm.
         *
         * discountValue:
         * 20 = 20%
         *
         * subtotal:
         * 1,000,000
         *
         * discount:
         * 200,000
         */
        public BigDecimal calculateDiscount(
                DiscountCode discountCode,
                BigDecimal subtotal
        ) {

            if (subtotal == null
                    || subtotal.compareTo(
                    BigDecimal.ZERO
            ) < 0) {

                throw new RuntimeException(
                        "Subtotal không hợp lệ"
                );
            }

            if (!isValid(discountCode)) {
                throw new RuntimeException(
                        "Mã giảm giá không hợp lệ"
                );
            }

            BigDecimal percentage =
                    discountCode.getDiscountValue()
                            .divide(
                                    BigDecimal.valueOf(100)
                            );

            BigDecimal discount =
                    subtotal.multiply(percentage);

            /*
             * Không cho discount vượt quá subtotal.
             */
            if (discount.compareTo(subtotal) > 0) {
                return subtotal;
            }

            return discount;
        }

        /*
         * ============================
         * MANAGER
         * ============================
         */

        /*
         * Tạo Discount Code.
         */
        @Transactional
        public DiscountCode createDiscountCode(
                String code,
                BigDecimal discountValue,
                Integer quantity,
                LocalDateTime startAt,
                LocalDateTime endAt
        ) {

            validateDiscountData(
                    code,
                    discountValue,
                    quantity,
                    startAt,
                    endAt
            );

            String normalizedCode =
                    code.trim().toUpperCase();

            if (discountCodeRepository
                    .existsByCodeIgnoreCase(
                            normalizedCode
                    )) {

                throw new RuntimeException(
                        "Mã giảm giá đã tồn tại"
                );
            }

            DiscountCode discountCode =
                    DiscountCode.builder()
                            .code(normalizedCode)
                            .discountValue(discountValue)
                            .quantity(quantity)
                            .startAt(startAt)
                            .endAt(endAt)
                            .active(true)
                            .build();

            return discountCodeRepository.save(
                    discountCode
            );
        }

        /*
         * Cập nhật Discount Code.
         */
        @Transactional
        public DiscountCode updateDiscountCode(
                Long id,
                String code,
                BigDecimal discountValue,
                Integer quantity,
                LocalDateTime startAt,
                LocalDateTime endAt
        ) {

            validateDiscountData(
                    code,
                    discountValue,
                    quantity,
                    startAt,
                    endAt
            );

            DiscountCode discountCode =
                    discountCodeRepository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy Discount Code"
                                    )
                            );

            String normalizedCode =
                    code.trim().toUpperCase();

            Optional<DiscountCode> existing =
                    discountCodeRepository
                            .findByCodeIgnoreCase(
                                    normalizedCode
                            );

            if (existing.isPresent()
                    && !existing.get()
                    .getId()
                    .equals(id)) {

                throw new RuntimeException(
                        "Mã giảm giá đã tồn tại"
                );
            }

            discountCode.setCode(
                    normalizedCode
            );

            discountCode.setDiscountValue(
                    discountValue
            );

            discountCode.setQuantity(
                    quantity
            );

            discountCode.setStartAt(
                    startAt
            );

            discountCode.setEndAt(
                    endAt
            );

            return discountCodeRepository.save(
                    discountCode
            );
        }

        /*
         * Bật / tắt Discount Code.
         */
        @Transactional
        public DiscountCode updateActive(
                Long id,
                boolean active
        ) {

            DiscountCode discountCode =
                    discountCodeRepository.findById(id)
                            .orElseThrow(() ->
                                    new RuntimeException(
                                            "Không tìm thấy Discount Code"
                                    )
                            );

            discountCode.setActive(active);

            return discountCodeRepository.save(
                    discountCode
            );
        }

        /*
         * Xóa Discount Code.
         */
        @Transactional
        public void deleteDiscountCode(
                Long id
        ) {

            if (!discountCodeRepository.existsById(id)) {
                throw new RuntimeException(
                        "Không tìm thấy Discount Code"
                );
            }

            discountCodeRepository.deleteById(id);
        }

        /*
         * ============================
         * USE CODE
         * ============================
         */

        /*
         * Trừ 1 lượt sử dụng.
         *
         * Method này được OrderService gọi
         * trong transaction checkout.
         */
        public void decreaseQuantity(
                DiscountCode discountCode
        ) {

            if (!isValid(discountCode)) {
                throw new RuntimeException(
                        "Mã giảm giá không hợp lệ"
                );
            }

            int quantity =
                    discountCode.getQuantity();

            if (quantity <= 0) {
                throw new RuntimeException(
                        "Mã giảm giá đã hết lượt sử dụng"
                );
            }

            discountCode.setQuantity(
                    quantity - 1
            );

            discountCodeRepository.save(
                    discountCode
            );
        }

        /*
         * ============================
         * PRIVATE
         * ============================
         */

        private void validateDiscountData(
                String code,
                BigDecimal discountValue,
                Integer quantity,
                LocalDateTime startAt,
                LocalDateTime endAt
        ) {

            if (code == null
                    || code.trim().isEmpty()) {

                throw new RuntimeException(
                        "Code không được để trống"
                );
            }

            if (discountValue == null
                    || discountValue.compareTo(
                    BigDecimal.ZERO
            ) <= 0
                    || discountValue.compareTo(
                    BigDecimal.valueOf(100)
            ) > 0) {

                throw new RuntimeException(
                        "Mức giảm phải từ 0 đến 100%"
                );
            }

            if (quantity == null || quantity <= 0) {

                throw new RuntimeException(
                        "Quantity phải lớn hơn 0"
                );
            }

            if (startAt == null || endAt == null) {

                throw new RuntimeException(
                        "Thời gian không được để trống"
                );
            }

            if (!endAt.isAfter(startAt)) {

                throw new RuntimeException(
                        "End time phải sau Start time"
                );
            }
        }
    }