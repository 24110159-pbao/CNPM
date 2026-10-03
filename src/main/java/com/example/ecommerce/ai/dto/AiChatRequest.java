package com.example.ecommerce.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AiChatRequest(
	@NotBlank(message = "Vui lòng nhập câu hỏi.")
	@Size(max = 1000, message = "Câu hỏi không được vượt quá 1000 ký tự.")
	String question
) {
}
