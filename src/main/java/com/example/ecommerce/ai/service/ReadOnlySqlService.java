package com.example.ecommerce.ai.service;

import com.example.ecommerce.ai.dto.SqlQueryResult;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReadOnlySqlService {

	private static final int MAX_ROWS = 100;

	private final JdbcTemplate jdbcTemplate;

	@Transactional(readOnly = true, timeout = 15)
	public SqlQueryResult executeSelect(String validatedSql) {

		List<Map<String, Object>> rows =
				jdbcTemplate.query(
						validatedSql,

						statement -> {
							statement.setQueryTimeout(12);
							statement.setMaxRows(MAX_ROWS);
							statement.setFetchSize(MAX_ROWS);
						},

						resultSet -> {

							ResultSetMetaData metadata =
									resultSet.getMetaData();

							int columnCount =
									metadata.getColumnCount();

							List<Map<String, Object>> result =
									new ArrayList<>();

							while (
									resultSet.next()
											&& result.size() < MAX_ROWS
							) {

								Map<String, Object> row =
										new LinkedHashMap<>();

								for (
										int column = 1;
										column <= columnCount;
										column++
								) {

									String columnName =
											metadata.getColumnLabel(column);

									String safeName =
											columnName
													.toLowerCase(
															Locale.ROOT
													);

									/*
									 * Defense-in-depth.
									 */
									if (
											safeName.contains("password")
													|| safeName.contains("otp")
													|| safeName.contains("secret")
													|| safeName.contains("token")
									) {
										continue;
									}

									row.put(
											columnName,
											resultSet.getObject(column)
									);
								}

								result.add(row);
							}

							return result;
						}
				);

		return new SqlQueryResult(rows);
	}
}
