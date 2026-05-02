package io.fnr.core.domain;

import java.time.Instant;

public class TaskRecord {
    private final String ticketId;
    private final String jobName;
    private final long timeoutSeconds;
    private final Instant startedAt;
    private volatile TaskStatus status;
    private volatile Instant completedAt;
    private volatile String resultPayload;
    private volatile String paramPayload;
    private volatile String errorMessage;

    private TaskRecord(Builder b) {
        this.ticketId     = b.ticketId;
        this.jobName      = b.jobName;
        this.timeoutSeconds = b.timeoutSeconds;
        this.startedAt    = b.startedAt;
        this.status       = b.status;
        this.paramPayload = b.paramPayload;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String ticketId;
        private String jobName;
        private long timeoutSeconds;
        private Instant startedAt;
        private TaskStatus status = TaskStatus.PENDING;
        private String paramPayload;

        public Builder ticketId(String v)      { this.ticketId = v; return this; }
        public Builder jobName(String v)       { this.jobName = v; return this; }
        public Builder timeoutSeconds(long v)  { this.timeoutSeconds = v; return this; }
        public Builder startedAt(Instant v)    { this.startedAt = v; return this; }
        public Builder status(TaskStatus v)    { this.status = v; return this; }
        public Builder paramPayload(String v)  { this.paramPayload = v; return this; }
        public TaskRecord build()              { return new TaskRecord(this); }
    }

    public String getTicketId()       { return ticketId; }
    public String getJobName()        { return jobName; }
    public long getTimeoutSeconds()   { return timeoutSeconds; }
    public Instant getStartedAt()     { return startedAt; }
    public TaskStatus getStatus()     { return status; }
    public Instant getCompletedAt()   { return completedAt; }
    public String getResultPayload()  { return resultPayload; }
    public String getParamPayload()   { return paramPayload; }
    public String getErrorMessage()   { return errorMessage; }

    public void setStatus(TaskStatus v)       { this.status = v; }
    public void setCompletedAt(Instant v)     { this.completedAt = v; }
    public void setResultPayload(String v)    { this.resultPayload = v; }
    public void setParamPayload(String v)     { this.paramPayload = v; }
    public void setErrorMessage(String v)     { this.errorMessage = v; }
}
