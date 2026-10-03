package com.example.ecommerce.ai.service;

import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class SqlValidationService {

	private static final int MAX_LIMIT = 100;
	private static final Set<String> ALLOWED_TABLES = Set.of(
			"users", "categories", "products", "product_specs", "carts",
			"cart_items", "orders", "order_items", "payments", "discount_codes",
			"reviews", "notifications"
	);
	private static final Pattern SOURCE_PATTERN = Pattern.compile(
			"(?i)\\b(?:FROM|JOIN)\\s+([`\\w.]+)(?:\\s+(?:AS\\s+)?([a-zA-Z_][\\w]*))?"
	);
	private static final Pattern LIMIT_PATTERN = Pattern.compile(
			"(?i)\\bLIMIT\\s+(\\d+)(?:\\s*,\\s*(\\d+))?(?:\\s+OFFSET\\s+(\\d+))?\\b"
	);
	private static final Pattern BLOCKED_PATTERN = Pattern.compile(
			"(?i)\\b(UNION|INTO|OUTFILE|DUMPFILE|PROCEDURE|EXEC|EXECUTE|CALL|" +
					"INSERT|UPDATE|DELETE|DROP|ALTER|TRUNCATE|CREATE|GRANT|REVOKE|" +
					"SLEEP|BENCHMARK|LOAD_FILE|GET_LOCK|RELEASE_LOCK|IS_FREE_LOCK|" +
					"IS_USED_LOCK|INFORMATION_SCHEMA|PERFORMANCE_SCHEMA|SYS|MYSQL|" +
					"PASSWORD|PASSWORD_HASH|OTP|SECRET|API_KEY|TOKEN|LOCK)\\b"
	);

	public String validate(String sql) {
		if (sql == null || sql.isBlank() || sql.length() > 10_000) {
			throw new IllegalArgumentException("empty or oversized SQL");
		}

		String normalized = sql.trim();
		if (!normalized.regionMatches(true, 0, "SELECT", 0, 6)
				|| (normalized.length() > 6 && Character.isLetterOrDigit(normalized.charAt(6)))) {
			throw new IllegalArgumentException("only SELECT is allowed");
		}

		if (normalized.contains(";") || normalized.contains("--") || normalized.contains("#")
				|| normalized.contains("/*") || normalized.contains("*/")
			|| normalized.contains("@")) {
			throw new IllegalArgumentException("comments, variables and multiple statements are not allowed");
		}

		String lower = normalized.toLowerCase(Locale.ROOT);
		if (BLOCKED_PATTERN.matcher(lower).find() || lower.contains(":=")) {
			throw new IllegalArgumentException("unsafe SQL token");
		}

		// Reject SELECT * but allow aggregate forms such as COUNT(*).
		String withoutCountStar = lower.replaceAll("(?i)count\\s*\\(\\s*\\*\\s*\\)", "count(1)");
		if (Pattern.compile("(?i)(^select\\s+\\*|,\\s*\\*|\\b[a-z_][\\w]*\\.\\*)")
				.matcher(withoutCountStar).find()) {
			throw new IllegalArgumentException("select only the columns needed");
		}

		validateTables(normalized);
		validateLimit(normalized);

		try {
			Statement statement = CCJSqlParserUtil.parse(normalized);
			if (!(statement instanceof Select)) {
				throw new IllegalArgumentException("only SELECT is allowed");
			}
		} catch (IllegalArgumentException exception) {
			throw exception;
		} catch (Exception exception) {
			throw new IllegalArgumentException("invalid SQL syntax", exception);
		}
		return normalized;
	}

	private void validateTables(String sql) {
		Matcher matcher = SOURCE_PATTERN.matcher(sql);
		boolean foundSource = false;
		while (matcher.find()) {
			foundSource = true;
			String table = matcher.group(1).replace("`", "").toLowerCase(Locale.ROOT);
			if (table.contains(".") || !ALLOWED_TABLES.contains(table)) {
				throw new IllegalArgumentException("table is not available to AI queries");
			}
		}
		if (!foundSource) {
			throw new IllegalArgumentException("query must read an approved table");
		}
		if (Pattern.compile("(?i)\\bFROM\\s*\\(|\\bJOIN\\s*\\(").matcher(sql).find()) {
			throw new IllegalArgumentException("derived tables are not allowed");
		}
		if (Pattern.compile("(?i)\\bFROM\\s+[\\w`]+(?:\\s+(?:AS\\s+)?[\\w]+)?\\s*,\\s*[\\w`]+")
				.matcher(sql).find()) {
			throw new IllegalArgumentException("comma joins are not allowed");
		}
	}

	private void validateLimit(String sql) {
		Matcher matcher = LIMIT_PATTERN.matcher(sql);
		if (!matcher.find()) {
			throw new IllegalArgumentException("LIMIT is required");
		}
		String requestedRows = matcher.group(2) != null ? matcher.group(2) : matcher.group(1);
		if (Integer.parseInt(requestedRows) > MAX_LIMIT) {
			throw new IllegalArgumentException("LIMIT exceeds the maximum row count");
		}
		if (matcher.find()) {
			throw new IllegalArgumentException("multiple LIMIT clauses are not allowed");
		}
	}
}
