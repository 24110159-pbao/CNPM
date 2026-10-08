package com.example.ecommerce.ai.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlValidationServiceTest {

    private final SqlValidationService validator = new SqlValidationService();

    @Test
    void acceptsBoundedSelectFromApprovedTables() {
        String sql = "SELECT COUNT(*) FROM users LIMIT 1";

        assertEquals(sql, validator.validate(sql));
    }

    @Test
    void acceptsJoinOnApprovedTables() {
        String sql = "SELECT p.name, SUM(oi.quantity) AS sold FROM products p " +
                "JOIN order_items oi ON oi.product_id = p.id GROUP BY p.id LIMIT 10";

        assertEquals(sql, validator.validate(sql));
    }

    @Test
    void rejectsEveryRequiredWriteAttack() {
        for (String sql : new String[]{
                "DELETE FROM users",
                "UPDATE products SET price = 0",
                "DROP TABLE users",
                "TRUNCATE orders"
        }) {
            assertThrows(IllegalArgumentException.class, () -> validator.validate(sql), sql);
        }
    }

    @Test
    void rejectsMultipleStatementsCommentsAndSensitiveColumns() {
        for (String sql : new String[]{
                "SELECT id FROM users LIMIT 1; DELETE FROM users",
                "SELECT id FROM users /* bypass */ LIMIT 1",
                "SELECT password FROM users LIMIT 1"
        }) {
            assertThrows(IllegalArgumentException.class, () -> validator.validate(sql), sql);
        }
    }

    @Test
    void rejectsUnapprovedTablesCrossSchemaAndMissingOrExcessiveLimit() {
        for (String sql : new String[]{
                "SELECT otp FROM otp_verifications LIMIT 1",
                "SELECT id FROM other_db.users LIMIT 1",
                "SELECT id FROM users",
                "SELECT id FROM users LIMIT 101"
        }) {
            assertThrows(IllegalArgumentException.class, () -> validator.validate(sql), sql);
        }
    }
}