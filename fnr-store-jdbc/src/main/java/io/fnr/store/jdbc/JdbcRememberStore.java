package io.fnr.store.jdbc;

import io.fnr.core.domain.TaskRecord;
import io.fnr.core.domain.TaskStatus;
import io.fnr.core.store.RememberStore;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSetMetaData;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JdbcRememberStore implements RememberStore {

    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<TaskRecord> rowMapper;

    private final String sqlSave;
    private final String sqlFindById;
    private final String sqlUpdateStatus;
    private final String sqlUpdateSuccess;
    private final String sqlUpdateFailed;

    public JdbcRememberStore(JdbcTemplate jdbcTemplate, JdbcStoreConfig cfg) {
        this.jdbcTemplate = jdbcTemplate;
        if (!cfg.isDefault()) validateSchema(jdbcTemplate, cfg);
        this.sqlSave        = buildSave(cfg);
        this.sqlFindById    = buildFindById(cfg);
        this.sqlUpdateStatus  = buildUpdateStatus(cfg);
        this.sqlUpdateSuccess = buildUpdateSuccess(cfg);
        this.sqlUpdateFailed  = buildUpdateFailed(cfg);
        this.rowMapper      = buildRowMapper(cfg);
    }

    // ── query builders ──────────────────────────────────────────────────────

    private static String buildSave(JdbcStoreConfig c) {
        return "INSERT INTO " + c.getTableName() + " ("
            + c.getColTicketId()       + ", "
            + c.getColJobName()        + ", "
            + c.getColStatus()         + ", "
            + c.getColStartedAt()      + ", "
            + c.getColTimeoutSeconds() + ", "
            + c.getColParamPayload()
            + ") VALUES (?, ?, ?, ?, ?, ?)";
    }

    private static String buildFindById(JdbcStoreConfig c) {
        return "SELECT * FROM " + c.getTableName()
            + " WHERE " + c.getColTicketId() + " = ?";
    }

    private static String buildUpdateStatus(JdbcStoreConfig c) {
        return "UPDATE " + c.getTableName()
            + " SET " + c.getColStatus() + " = ?"
            + " WHERE " + c.getColTicketId() + " = ?";
    }

    private static String buildUpdateSuccess(JdbcStoreConfig c) {
        return "UPDATE " + c.getTableName() + " SET "
            + c.getColStatus()        + " = '" + TaskStatus.SUCCESS.name() + "', "
            + c.getColResultPayload() + " = ?, "
            + c.getColCompletedAt()   + " = NOW()"
            + " WHERE " + c.getColTicketId() + " = ?";
    }

    private static String buildUpdateFailed(JdbcStoreConfig c) {
        return "UPDATE " + c.getTableName() + " SET "
            + c.getColStatus()       + " = '" + TaskStatus.FAILED.name() + "', "
            + c.getColErrorMessage() + " = ?, "
            + c.getColCompletedAt()  + " = NOW()"
            + " WHERE " + c.getColTicketId() + " = ?";
    }

    private static RowMapper<TaskRecord> buildRowMapper(JdbcStoreConfig c) {
        return (rs, rowNum) -> {
            TaskRecord r = TaskRecord.builder()
                .ticketId(rs.getString(c.getColTicketId()))
                .jobName(rs.getString(c.getColJobName()))
                .status(TaskStatus.valueOf(rs.getString(c.getColStatus())))
                .startedAt(rs.getTimestamp(c.getColStartedAt()).toInstant())
                .timeoutSeconds(rs.getLong(c.getColTimeoutSeconds()))
                .build();
            Timestamp completedAt = rs.getTimestamp(c.getColCompletedAt());
            if (completedAt != null) r.setCompletedAt(completedAt.toInstant());
            r.setResultPayload(rs.getString(c.getColResultPayload()));
            r.setParamPayload(rs.getString(c.getColParamPayload()));
            r.setErrorMessage(rs.getString(c.getColErrorMessage()));
            return r;
        };
    }

    // ── schema ──────────────────────────────────────────────────────────────

    public static String generateSchema(JdbcStoreConfig c) {
        String t = c.getTableName();
        return "CREATE TABLE IF NOT EXISTS " + t + " (\n"
            + "    " + c.getColTicketId()       + "       VARCHAR(36)  NOT NULL PRIMARY KEY,\n"
            + "    " + c.getColJobName()         + "         VARCHAR(255) NOT NULL,\n"
            + "    " + c.getColStatus()          + "            VARCHAR(20)  NOT NULL,\n"
            + "    " + c.getColStartedAt()       + "        TIMESTAMP    NOT NULL,\n"
            + "    " + c.getColCompletedAt()     + "      TIMESTAMP,\n"
            + "    " + c.getColTimeoutSeconds()  + "   BIGINT       NOT NULL,\n"
            + "    " + c.getColResultPayload()   + "    TEXT,\n"
            + "    " + c.getColParamPayload()    + "     TEXT,\n"
            + "    " + c.getColErrorMessage()    + "     TEXT\n"
            + ");\n\n"
            + "CREATE INDEX IF NOT EXISTS idx_" + t + "_status"
            + "   ON " + t + "(" + c.getColStatus() + ");\n"
            + "CREATE INDEX IF NOT EXISTS idx_" + t + "_job_name"
            + " ON " + t + "(" + c.getColJobName() + ");\n";
    }

    private static void validateSchema(JdbcTemplate jdbcTemplate, JdbcStoreConfig c) {
        Map<String, Integer> actual = new HashMap<>();
        try {
            jdbcTemplate.query(
                "SELECT * FROM " + c.getTableName() + " WHERE 1=0",
                rs -> {
                    ResultSetMetaData meta = rs.getMetaData();
                    for (int i = 1; i <= meta.getColumnCount(); i++) {
                        actual.put(meta.getColumnName(i).toLowerCase(), meta.getColumnType(i));
                    }
                    return null;
                }
            );
        } catch (Exception e) {
            throw new IllegalStateException(
                "Table '" + c.getTableName() + "' not found or not accessible: " + e.getMessage(), e
            );
        }

        for (ColumnDef col : c.columnDefs()) {
            String colName = col.name().toLowerCase();
            if (!actual.containsKey(colName)) {
                throw new IllegalStateException(
                    "Column '" + col.name() + "' not found in table '" + c.getTableName() + "'"
                );
            }
            if (!col.acceptedTypes().contains(actual.get(colName))) {
                throw new IllegalStateException(
                    "Column '" + col.name() + "' in table '" + c.getTableName()
                    + "' has incompatible type (JDBC type code: " + actual.get(colName) + ")"
                );
            }
        }
    }

    // ── RememberStore ────────────────────────────────────────────────────────

    @Override
    public void save(TaskRecord record) {
        jdbcTemplate.update(sqlSave,
            record.getTicketId(),
            record.getJobName(),
            record.getStatus().name(),
            Timestamp.from(record.getStartedAt()),
            record.getTimeoutSeconds(),
            record.getParamPayload()
        );
    }

    @Override
    public Optional<TaskRecord> findByTicketId(String ticketId) {
        var results = jdbcTemplate.query(sqlFindById, rowMapper, ticketId);
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    @Override
    public void updateStatus(String ticketId, TaskStatus status) {
        jdbcTemplate.update(sqlUpdateStatus, status.name(), ticketId);
    }

    @Override
    public void updateSuccess(String ticketId, String resultPayload) {
        jdbcTemplate.update(sqlUpdateSuccess, resultPayload, ticketId);
    }

    @Override
    public void updateFailed(String ticketId, String errorMessage) {
        jdbcTemplate.update(sqlUpdateFailed, errorMessage, ticketId);
    }
}
