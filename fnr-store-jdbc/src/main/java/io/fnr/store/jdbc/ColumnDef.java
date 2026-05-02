package io.fnr.store.jdbc;

import java.sql.Types;
import java.util.Set;

record ColumnDef(String name, Set<Integer> acceptedTypes) {

    static final Set<Integer> TEXT      = Set.of(Types.VARCHAR, Types.CHAR, Types.NVARCHAR,
                                                  Types.LONGVARCHAR, Types.LONGNVARCHAR, Types.CLOB);
    static final Set<Integer> TIMESTAMP = Set.of(Types.TIMESTAMP, Types.TIMESTAMP_WITH_TIMEZONE);
    static final Set<Integer> NUMERIC   = Set.of(Types.BIGINT, Types.INTEGER, Types.NUMERIC,
                                                  Types.DECIMAL, Types.SMALLINT);
}
