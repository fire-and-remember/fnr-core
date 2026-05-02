package io.fnr.store.jdbc;

import java.util.List;
import java.util.regex.Pattern;

public class JdbcStoreConfig {

    private static final Pattern VALID_IDENTIFIER = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    private final String tableName;
    private final String colTicketId;
    private final String colJobName;
    private final String colStatus;
    private final String colStartedAt;
    private final String colTimeoutSeconds;
    private final String colParamPayload;
    private final String colResultPayload;
    private final String colErrorMessage;
    private final String colCompletedAt;

    private JdbcStoreConfig(Builder builder) {
        this.tableName         = validated(builder.tableName,         "tableName");
        this.colTicketId       = validated(builder.colTicketId,       "colTicketId");
        this.colJobName        = validated(builder.colJobName,        "colJobName");
        this.colStatus         = validated(builder.colStatus,         "colStatus");
        this.colStartedAt      = validated(builder.colStartedAt,      "colStartedAt");
        this.colTimeoutSeconds = validated(builder.colTimeoutSeconds, "colTimeoutSeconds");
        this.colParamPayload   = validated(builder.colParamPayload,   "colParamPayload");
        this.colResultPayload  = validated(builder.colResultPayload,  "colResultPayload");
        this.colErrorMessage   = validated(builder.colErrorMessage,   "colErrorMessage");
        this.colCompletedAt    = validated(builder.colCompletedAt,    "colCompletedAt");
    }

    private static String validated(String value, String fieldName) {
        if (!VALID_IDENTIFIER.matcher(value).matches()) {
            throw new IllegalArgumentException(
                "Invalid SQL identifier for " + fieldName + ": '" + value + "'"
            );
        }
        return value;
    }

    public static Builder builder() { return new Builder(); }

    public static JdbcStoreConfig defaults() { return builder().build(); }

    public boolean isDefault() {
        return tableName.equals("fnr_tasks")
            && colTicketId.equals("ticket_id")
            && colJobName.equals("job_name")
            && colStatus.equals("status")
            && colStartedAt.equals("started_at")
            && colTimeoutSeconds.equals("timeout_seconds")
            && colParamPayload.equals("param_payload")
            && colResultPayload.equals("result_payload")
            && colErrorMessage.equals("error_message")
            && colCompletedAt.equals("completed_at");
    }

    public List<ColumnDef> columnDefs() {
        return List.of(
            new ColumnDef(colTicketId,       ColumnDef.TEXT),
            new ColumnDef(colJobName,        ColumnDef.TEXT),
            new ColumnDef(colStatus,         ColumnDef.TEXT),
            new ColumnDef(colStartedAt,      ColumnDef.TIMESTAMP),
            new ColumnDef(colCompletedAt,    ColumnDef.TIMESTAMP),
            new ColumnDef(colTimeoutSeconds, ColumnDef.NUMERIC),
            new ColumnDef(colParamPayload,   ColumnDef.TEXT),
            new ColumnDef(colResultPayload,  ColumnDef.TEXT),
            new ColumnDef(colErrorMessage,   ColumnDef.TEXT)
        );
    }

    public String getTableName()         { return tableName; }
    public String getColTicketId()       { return colTicketId; }
    public String getColJobName()        { return colJobName; }
    public String getColStatus()         { return colStatus; }
    public String getColStartedAt()      { return colStartedAt; }
    public String getColTimeoutSeconds() { return colTimeoutSeconds; }
    public String getColParamPayload()   { return colParamPayload; }
    public String getColResultPayload()  { return colResultPayload; }
    public String getColErrorMessage()   { return colErrorMessage; }
    public String getColCompletedAt()    { return colCompletedAt; }

    public static class Builder {
        private String tableName         = "fnr_tasks";
        private String colTicketId       = "ticket_id";
        private String colJobName        = "job_name";
        private String colStatus         = "status";
        private String colStartedAt      = "started_at";
        private String colTimeoutSeconds = "timeout_seconds";
        private String colParamPayload   = "param_payload";
        private String colResultPayload  = "result_payload";
        private String colErrorMessage   = "error_message";
        private String colCompletedAt    = "completed_at";

        public Builder tableName(String name)         { this.tableName = name; return this; }
        public Builder colTicketId(String name)       { this.colTicketId = name; return this; }
        public Builder colJobName(String name)        { this.colJobName = name; return this; }
        public Builder colStatus(String name)         { this.colStatus = name; return this; }
        public Builder colStartedAt(String name)      { this.colStartedAt = name; return this; }
        public Builder colTimeoutSeconds(String name) { this.colTimeoutSeconds = name; return this; }
        public Builder colParamPayload(String name)   { this.colParamPayload = name; return this; }
        public Builder colResultPayload(String name)  { this.colResultPayload = name; return this; }
        public Builder colErrorMessage(String name)   { this.colErrorMessage = name; return this; }
        public Builder colCompletedAt(String name)    { this.colCompletedAt = name; return this; }
        public JdbcStoreConfig build()             { return new JdbcStoreConfig(this); }
    }
}
