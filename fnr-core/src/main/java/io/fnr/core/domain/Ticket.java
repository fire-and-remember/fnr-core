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
 * @param <T> the expected type of the task's return value
 */
public class Ticket<T> {
    private final String ticketId;
    private final String jobName;

    public Ticket(String ticketId, String jobName) {
        this.ticketId = ticketId;
        this.jobName = jobName;
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
    public String getJobName()  { return jobName; }
}
