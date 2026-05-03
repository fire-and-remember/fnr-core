package io.fnr.core.domain;

/**
 * Lifecycle states of an async task tracked by FireAndRemember.
 */
public enum TaskStatus {

    /** The task has been submitted and persisted, but execution has not started yet. */
    PENDING,

    /** The task is currently being executed. */
    RUNNING,

    /** The task completed successfully. The result is available via {@link TicketResult#getValue()}. */
    SUCCESS,

    /** The task failed or timed out. The reason is available via {@link TicketResult#getErrorMessage()}. */
    FAILED
}
