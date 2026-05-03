package io.fnr.core.domain;

/**
 * A lightweight receipt for a submitted async task.
 *
 * <p>Returned immediately when a method annotated with {@code @Remember} is called.
 * Use {@link #getTicketId()} to retrieve the task result later via
 * {@link io.fnr.core.service.FnrTicketService}.
 *
 * <pre>{@code
 * Ticket<OrderResult> ticket = orderService.processOrder(request);
 * String ticketId = ticket.getTicketId(); // persist or return to the caller
 * }</pre>
 *
 * <p>Inside a {@code @Remember} method body, use {@link #of(Object)} to pass the computed
 * result back to the framework:
 *
 * <pre>{@code
 * @Remember(jobName = "process-order")
 * public Ticket<OrderResult> processOrder(OrderRequest req) {
 *     OrderResult result = orderService.process(req);
 *     return Ticket.of(result);
 * }
 * }</pre>
 *
 * @param <T> the expected type of the task's return value
 */
public class Ticket<T> {
    private final String ticketId;
    private final String jobName;
    private final T value;

    public Ticket(String ticketId, String jobName) {
        this.ticketId = ticketId;
        this.jobName = jobName;
        this.value = null;
    }

    private Ticket(T value) {
        this.ticketId = null;
        this.jobName = null;
        this.value = value;
    }

    /**
     * Creates a result-carrying ticket to be returned from a {@code @Remember} method body.
     *
     * <p>The framework extracts {@code value} and stores it as the task result.
     * This is the only way to persist a return value when using the {@code @Remember} annotation.
     *
     * @param value the computed result; may be {@code null}
     * @return a ticket carrying the result value
     * @param <T> the result type
     */
    public static <T> Ticket<T> of(T value) {
        return new Ticket<>(value);
    }

    /**
     * Returns the unique identifier for this task.
     *
     * <p>Pass this ID to {@link io.fnr.core.service.FnrTicketService#getResult} or
     * {@link io.fnr.core.service.FnrTicketService#waitForResult} to check or await the result.
     *
     * @return a UUID string identifying this task
     */
    public String getTicketId() { return ticketId; }

    /**
     * Returns the logical job name declared on the {@code @Remember} annotation.
     *
     * @return the job name
     */
    public String getJobName() { return jobName; }

    /**
     * Returns the result value carried by this ticket.
     *
     * <p>Only meaningful on tickets created via {@link #of(Object)} inside a {@code @Remember}
     * method body. On caller-side tickets (returned by the framework), this is always {@code null}.
     *
     * @return the result value, or {@code null}
     */
    public T getValue() { return value; }
}
