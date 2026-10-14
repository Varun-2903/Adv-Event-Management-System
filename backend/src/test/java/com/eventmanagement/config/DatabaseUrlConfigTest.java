package com.eventmanagement.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseUrlConfigTest {

    @Test
    void convertsRenderPostgresUrlToJdbcAndRemovesEmbeddedCredentials() {
        assertEquals(
                "jdbc:postgresql://db.example.com:5432/events?sslmode=require",
                DatabaseUrlConfig.toJdbcUrl(
                        "postgresql://db-user:db-password@db.example.com:5432/events?sslmode=require"));
    }

    @Test
    void convertsPostgresSchemeAliasToJdbc() {
        assertEquals(
                "jdbc:postgresql://db.example.com/events",
                DatabaseUrlConfig.toJdbcUrl("postgres://db.example.com/events"));
    }

    @Test
    void leavesJdbcUrlsUnchanged() {
        String url = "jdbc:mysql://localhost:3306/events";
        assertEquals(url, DatabaseUrlConfig.toJdbcUrl(url));
    }

    @Test
    void rejectsPostgresUrlsWithoutAHost() {
        assertThrows(IllegalArgumentException.class,
                () -> DatabaseUrlConfig.toJdbcUrl("postgresql:///events"));
    }
}
