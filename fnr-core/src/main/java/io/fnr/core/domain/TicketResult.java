package io.fnr.core.domain;

/**
 * Represents the current state of an async task, returned by {@link io.fnr.core.service.FnrTicketService}.
 *
 * <p>A result is immutable. Its {@link #getStatus() status} reflects the task lifecycle:
 * {@code PENDING → RUNNING → SUCCESS} or {@code FAILED}.
 *
 * <pre>{@code
 * TicketResult<OrderResult> result = fnrTicketService.getResult(ticketId, OrderResult.class);
 *
 * if (result.isCompleted()) {
 *     if (result.getStatus() == TaskStatus.SUCCESS) {
 *         OrderResult order = result.getValue();
 *     } else {
 *         String error = result.getErrorMessage();
 *     }
 * }
 * }</pre>
 *
 * @param <T> the type of the task's return value
 */
public class TicketResult<T> {
    private final TaskStatus status;
    private final T result;
    private final String errorMessage;
    private final String paramPayload;

    private TicketResult(TaskStatus status, T result, String errorMessage, String paramPayload) {
        this.status = status;
        this.result = result;
        this.errorMessage = errorMessage;
        this.paramPayload = paramPayload;
    }

    public static <T> TicketResult<T> pending()          { return new TicketResult<>(TaskStatus.PENDING, null, null, null); }
    public static <T> TicketResult<T> running()          { return new TicketResult<>(TaskStatus.RUNNING, null, null, null); }
    public static <T> TicketResult<T> success(T result)  { return new TicketResult<>(TaskStatus.SUCCESS, result, null, null); }
    public static <T> TicketResult<T> failed(String msg) { return new TicketResult<>(TaskStatus.FAILED, null, msg, null); }

    /**
     * Returns a copy of this result with the given parameter payload attached.
     *
     * @param paramPayload JSON-serialized parameters, or {@code null}
     * @return a new {@code TicketResult} with the same status and value
     */
    public TicketResult<T> withParamPayload(String paramPayload) {
        return new TicketResult<>(this.status, this.result, this.errorMessage, paramPayload);
    }

    /**
     * Returns the current status of the task.
     *
     * @return one of {@link TaskStatus#PENDING}, {@link TaskStatus#RUNNING},
     *         {@link TaskStatus#SUCCESS}, or {@link TaskStatus#FAILED}
     */
    public TaskStatus getStatus()      { return status; }

    /**
     * Returns the deserialized task result.
     *
     * <p>Returns {@code null} if the task has not yet succeeded, if {@code storeResult} is
     * {@code false}, or if the result type was {@code Void}.
     *
     * @return the task return value, or {@code null}
     */
    public T getValue()                { return result; }

    /**
     * Returns the error message if the task failed.
     *
     * <p>Returns {@code null} unless the task status is {@link TaskStatus#FAILED}.
     *
     * @return the error message, or {@code null}
     */
    public String getErrorMessage()    { return errorMessage; }

    /**
     * Returns the JSON-serialized parameters that were passed to the task.
     *
     * <p>Returns {@code null} unless {@code storeParameters} was enabled in the configuration.
     *
     * @return JSON string of the original method arguments, or {@code null}
     */
    public String getParamPayload()    { return paramPayload; }

    /**
     * Returns {@code true} if the task has finished, regardless of outcome.
     *
     * @return {@code true} if status is {@link TaskStatus#SUCCESS} or {@link TaskStatus#FAILED}
     */
    public boolean isCompleted()       { return status == TaskStatus.SUCCESS || status == TaskStatus.FAILED; }
}
