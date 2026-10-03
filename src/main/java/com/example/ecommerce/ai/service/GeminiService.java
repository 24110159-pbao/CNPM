package com.example.ecommerce.ai.service;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class GeminiService {

	private final RestClient geminiRestClient;

	@Value("${gemini.api.key:}")
	private String apiKey;

	@Value("${gemini.api.model:gemini-2.5-flash}")
	private String model;

	public String generate(String prompt) {

		if (apiKey == null || apiKey.isBlank()) {
			throw new IllegalStateException(
					"Gemini API key is not configured."
			);
		}

		Map<String, Object> request =
				Map.of(
						"contents",
						List.of(
								Map.of(
										"role",
										"user",
										"parts",
										List.of(
												Map.of(
														"text",
														prompt
												)
										)
								)
						),

						"generationConfig",
						Map.of(
								"temperature",
								0.1,

								"maxOutputTokens",
								1200
						)
				);

		JsonNode response =
				geminiRestClient
						.post()
						.uri(
								"/v1beta/models/{model}:generateContent",
								model
						)
						.header(
								"x-goog-api-key",
								apiKey
						)
						.contentType(
								MediaType.APPLICATION_JSON
						)
						.body(request)
						.retrieve()
						.body(JsonNode.class);

		JsonNode text =
				response == null
						? null
						: response
						.path("candidates")
						.path(0)
						.path("content")
						.path("parts")
						.path(0)
						.path("text");

		if (
				text == null
						|| !text.isTextual()
						|| text.asText().isBlank()
		) {
			throw new IllegalStateException(
					"Gemini returned no text response."
			);
		}

		return text.asText().trim();
	}
}
