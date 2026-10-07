package com.example.ecommerce.ai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class GeminiConfig {

	@Bean
	public RestClient.Builder restClientBuilder() {
		return RestClient.builder();
	}

	@Bean
	public RestClient geminiRestClient(
			RestClient.Builder builder
	) {
		return builder
				.baseUrl(
						"https://generativelanguage.googleapis.com"
				)
				.build();
	}
}
