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
			"users",
			"categories",
			"products",
			"product_specs",
			"carts",
			"cart_items",
			"orders",
			"order_items",
			"payments",
			"discount_codes",
			"reviews",
			"notifications"
	);

	private static final Pattern SOURCE_PATTERN = Pattern.compile(
			"(?i)\\b(?:FROM|JOIN)\\s+([`\\w.]+)"
	);

	private static final Pattern LIMIT_PATTERN = Pattern.compile(
			"(?i)\\bLIMIT\\s+(\\d+)(?:\\s*,\\s*(\\d+))?\\b"
	);

	private static final Pattern BLOCKED_PATTERN = Pattern.compile(
			"(?i)\\b(" +
					"UNION|" +
					"INTO|" +
					"OUTFILE|" +
					"DUMPFILE|" +
					"PROCEDURE|" +
					"EXEC|" +
					"EXECUTE|" +
					"CALL|" +
					"INSERT|" +
					"UPDATE|" +
					"DELETE|" +
					"DROP|" +
					"ALTER|" +
					"TRUNCATE|" +
					"CREATE|" +
					"GRANT|" +
					"REVOKE|" +
					"SLEEP|" +
					"BENCHMARK|" +
					"LOAD_FILE|" +
					"GET_LOCK|" +
					"RELEASE_LOCK|" +
					"IS_FREE_LOCK|" +
					"IS_USED_LOCK|" +
					"INFORMATION_SCHEMA|" +
					"PERFORMANCE_SCHEMA|" +
					"SYS|" +
					"MYSQL|" +
					"PASSWORD|" +
					"PASSWORD_HASH|" +
					"OTP|" +
					"SECRET|" +
					"API_KEY|" +
					"TOKEN|" +
					"LOCK" +
					")\\b"
	);

	public String validate(String sql) {

		if (sql == null || sql.isBlank()) {
			throw new IllegalArgumentException("SQL is empty.");
		}

		if (sql.length() > 10_000) {
			throw new IllegalArgumentException("SQL is too long.");
		}

		String normalized = sql.trim();

		/*
		 * Only SELECT may be the first statement.
		 */
		if (!normalized.regionMatches(
				true,
				0,
				"SELECT",
				0,
				6
		)) {
			throw new IllegalArgumentException(
					"Only SELECT is allowed."
			);
		}

		/*
		 * SELECTABC is not SELECT.
		 */
		if (normalized.length() > 6
				&& Character.isLetterOrDigit(normalized.charAt(6))) {

			throw new IllegalArgumentException(
					"Only SELECT is allowed."
			);
		}

		/*
		 * No semicolon.
		 * Therefore multi-statement is impossible.
		 */
		if (normalized.contains(";")) {
			throw new IllegalArgumentException(
					"Multiple statements are not allowed."
			);
		}

		/*
		 * No SQL comments.
		 */
		if (normalized.contains("--")
				|| normalized.contains("#")
				|| normalized.contains("/*")
				|| normalized.contains("*/")) {

			throw new IllegalArgumentException(
					"SQL comments are not allowed."
			);
		}

		/*
		 * No MySQL variables.
		 */
		if (normalized.contains("@")) {
			throw new IllegalArgumentException(
					"SQL variables are not allowed."
			);
		}

		String lower = normalized.toLowerCase(Locale.ROOT);

		/*
		 * Block dangerous operations and sensitive sources.
		 */
		if (BLOCKED_PATTERN.matcher(lower).find()) {
			throw new IllegalArgumentException(
					"Unsafe SQL token."
			);
		}

		/*
		 * MySQL assignment operator.
		 */
		if (lower.contains(":=")) {
			throw new IllegalArgumentException(
					"SQL assignment is not allowed."
			);
		}

		/*
		 * SELECT * is forbidden.
		 *
		 * COUNT(*) is allowed.
		 */
		String withoutCountStar = lower.replaceAll(
				"count\\s*\\(\\s*\\*\\s*\\)",
				"count(1)"
		);

		Pattern selectStarPattern = Pattern.compile(
				"(?i)" +
						"(^SELECT\\s+\\*)" +
						"|(,\\s*\\*)" +
						"|(\\b[a-z_][\\w]*\\.\\*)"
		);

		if (selectStarPattern
				.matcher(withoutCountStar)
				.find()) {

			throw new IllegalArgumentException(
					"SELECT * is not allowed."
			);
		}

		/*
		 * Validate tables.
		 */
		validateTables(normalized);

		/*
		 * Every query must have LIMIT.
		 */
		validateLimit(normalized);

		/*
		 * Parse SQL structurally.
		 */
		try {

			Statement statement =
					CCJSqlParserUtil.parse(normalized);

			if (!(statement instanceof Select)) {
				throw new IllegalArgumentException(
						"Only SELECT is allowed."
				);
			}

		} catch (IllegalArgumentException exception) {

			throw exception;

		} catch (Exception exception) {

			throw new IllegalArgumentException(
					"Invalid SQL syntax.",
					exception
			);
		}

		return normalized;
	}

	private void validateTables(String sql) {

		Matcher matcher =
				SOURCE_PATTERN.matcher(sql);

		boolean foundTable = false;

		while (matcher.find()) {

			foundTable = true;

			String table = matcher
					.group(1)
					.replace("`", "")
					.toLowerCase(Locale.ROOT);

			/*
			 * Prevent:
			 *
			 * database.users
			 * other_schema.users
			 */
			if (table.contains(".")) {
				throw new IllegalArgumentException(
						"Schema-qualified tables are not allowed."
				);
			}

			if (!ALLOWED_TABLES.contains(table)) {
				throw new IllegalArgumentException(
						"Table is not available to AI queries."
				);
			}
		}

		if (!foundTable) {
			throw new IllegalArgumentException(
					"Query must read an approved table."
			);
		}

		/*
		 * Derived tables/subqueries in FROM/JOIN
		 * are intentionally rejected to keep the
		 * security model simple.
		 */
		if (Pattern.compile(
				"(?i)\\bFROM\\s*\\("
		).matcher(sql).find()) {

			throw new IllegalArgumentException(
					"Derived tables are not allowed."
			);
		}

		if (Pattern.compile(
				"(?i)\\bJOIN\\s*\\("
		).matcher(sql).find()) {

			throw new IllegalArgumentException(
					"Derived tables are not allowed."
			);
		}

		/*
		 * Comma joins are rejected.
		 */
		if (Pattern.compile(
				"(?i)\\bFROM\\s+[\\w`]+\\s*,"
		).matcher(sql).find()) {

			throw new IllegalArgumentException(
					"Comma joins are not allowed."
			);
		}
	}

	private void validateLimit(String sql) {

		Matcher matcher =
				LIMIT_PATTERN.matcher(sql);

		if (!matcher.find()) {

			throw new IllegalArgumentException(
					"LIMIT is required."
			);
		}

		int rowCount;

		if (matcher.group(2) != null) {

			/*
			 * LIMIT offset, row_count
			 */
			rowCount =
					Integer.parseInt(matcher.group(2));

		} else {

			rowCount =
					Integer.parseInt(matcher.group(1));
		}

		if (rowCount <= 0 || rowCount > MAX_LIMIT) {

			throw new IllegalArgumentException(
					"LIMIT must be between 1 and 100."
			);
		}

		/*
		 * Only one LIMIT.
		 */
		if (matcher.find()) {

			throw new IllegalArgumentException(
					"Multiple LIMIT clauses are not allowed."
			);
		}
	}
}
