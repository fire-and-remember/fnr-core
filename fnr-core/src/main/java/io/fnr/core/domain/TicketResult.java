package io.fnr.core.domain;

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

    public TicketResult<T> withParamPayload(String paramPayload) {
        return new TicketResult<>(this.status, this.result, this.errorMessage, paramPayload);
    }

    public TaskStatus getStatus()      { return status; }
    public T getValue()                { return result; }
    public String getErrorMessage()    { return errorMessage; }
    public String getParamPayload()    { return paramPayload; }
    public boolean isCompleted()       { return status == TaskStatus.SUCCESS || status == TaskStatus.FAILED; }
}
