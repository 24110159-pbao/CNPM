package com.example.ecommerce.ai.dto;

import java.util.List;
import java.util.Map;

public record SqlQueryResult(List<Map<String, Object>> rows) {
	public SqlQueryResult {
		rows = List.copyOf(rows);
	}
}
