package io.orchestrai.jdbc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Set;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class FlywaySchemaTest {

    private static final Set<String> EXPECTED_TABLES = Set.of(
            "flows", "executions", "task_runs", "logs", "secrets", "triggers");

    @Inject
    DataSource dataSource;

    @Test
    void flywayCreatesAllTables() throws Exception {
        try (Connection connection = dataSource.getConnection()) {
            Set<String> tables = loadPublicTables(connection);
            EXPECTED_TABLES.forEach(table -> assertTrue(
                    tables.contains(table),
                    () -> "missing table: " + table + ", found: " + tables));

            assertEquals(6, countAppliedMigrations(connection));
        }
    }

    @Test
    void hibernateValidatesEntitiesAgainstSchema() {
        assertEquals(0L, io.orchestrai.jdbc.entity.FlowEntity.count());
    }

    private static Set<String> loadPublicTables(Connection connection) throws Exception {
        Set<String> tables = new HashSet<>();
        try (ResultSet rs = connection.getMetaData().getTables(
                null, "public", "%", new String[] {"TABLE"})) {
            while (rs.next()) {
                tables.add(rs.getString("TABLE_NAME").toLowerCase());
            }
        }
        return tables;
    }

    private static int countAppliedMigrations(Connection connection) throws Exception {
        try (ResultSet rs = connection.createStatement().executeQuery(
                "SELECT COUNT(*) FROM flyway_schema_history WHERE success = true")) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
