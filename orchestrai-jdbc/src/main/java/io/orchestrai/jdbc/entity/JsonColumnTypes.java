package io.orchestrai.jdbc.entity;

import org.hibernate.type.SqlTypes;

/**
 * Shared Hibernate 6 JSON type codes for PostgreSQL JSONB columns.
 */
public final class JsonColumnTypes {

    public static final int JSONB = SqlTypes.JSON;

    private JsonColumnTypes() {
    }
}
