package io.fnr.core.service;

import io.fnr.core.domain.TicketResult;

/**
 * User-facing service for retrieving async task results.
 *
 * <p>Inject this bean to look up the status and result of a task by its {@code ticketId}.
 * The {@code ticketId} is obtained from the {@link io.fnr.core.domain.Ticket} returned
 * by a method annotated with {@code @Remember}.
 *
 * <pre>{@code
 * Ticket<OrderResult> ticket = orderService.processOrder(request);
 * TicketResult<OrderResult> result = fnrTicketService.getResult(ticket.getTicketId(), OrderResult.class);
 * }</pre>
 */
public interface FnrTicketService {

    /**
     * Returns the current result of the task identified by {@code ticketId}.
     *
     * <p>This method is non-blocking. If the task has not yet completed,
     * the returned {@link TicketResult} will have status {@code PENDING} or {@code RUNNING}.
     *
     * @param ticketId   the ticket ID returned when the task was submitted
     * @param resultType the expected type of the task's return value; used for JSON deserialization
     * @param <T>        the result type
     * @return the current state of the task, never {@code null}
     * @throws io.fnr.core.exception.TicketNotFoundException if no task with the given ID exists
     * @throws IllegalArgumentException if the stored result payload cannot be deserialized into {@code resultType}
     */
    <T> TicketResult<T> getResult(String ticketId, Class<T> resultType);

    /**
     * Returns the current status of the task without deserializing the result.
     *
     * <p>Equivalent to {@code getResult(ticketId, Void.class)}.
     * Useful when you only need to check whether the task has completed.
     *
     * @param ticketId the ticket ID returned when the task was submitted
     * @return the current state of the task, never {@code null}
     * @throws io.fnr.core.exception.TicketNotFoundException if no task with the given ID exists
     */
    default TicketResult<Void> getResult(String ticketId) {
        return getResult(ticketId, Void.class);
    }

    /**
     * Polling interval used by {@link #waitForResult} between status checks, in milliseconds.
     */
    long WAIT_POLL_INTERVAL_MS = 500;

    /**
     * Blocks until the task completes or the given wait time elapses, then returns the result.
     *
     * <p>Polls every {@value #WAIT_POLL_INTERVAL_MS} ms. If the task does not complete
     * within {@code waitSeconds}, the last known state is returned — which may still be
     * {@code PENDING} or {@code RUNNING}.
     *
     * @param ticketId    the ticket ID returned when the task was submitted
     * @param resultType  the expected type of the task's return value
     * @param waitSeconds maximum number of seconds to wait
     * @param <T>         the result type
     * @return the task result; status is {@code SUCCESS} or {@code FAILED} if completed
     *         within the wait window, otherwise the last known status
     * @throws InterruptedException if the calling thread is interrupted while waiting
     * @throws io.fnr.core.exception.TicketNotFoundException if no task with the given ID exists
     */
    default <T> TicketResult<T> waitForResult(String ticketId, Class<T> resultType, long waitSeconds)
            throws InterruptedException {
        long deadline = System.currentTimeMillis() + waitSeconds * 1000;
        while (System.currentTimeMillis() < deadline) {
            TicketResult<T> result = getResult(ticketId, resultType);
            if (result.isCompleted()) return result;
            Thread.sleep(WAIT_POLL_INTERVAL_MS);
        }
        return getResult(ticketId, resultType);
    }

    /**
     * Blocks until the task completes or the given wait time elapses, without deserializing the result.
     *
     * <p>Equivalent to {@link #waitForResult(String, Class, long) waitForResult(ticketId, Void.class, waitSeconds)}.
     *
     * @param ticketId    the ticket ID returned when the task was submitted
     * @param waitSeconds maximum number of seconds to wait
     * @return the task result without a deserialized value
     * @throws InterruptedException if the calling thread is interrupted while waiting
     */
    default TicketResult<Void> waitForResult(String ticketId, long waitSeconds) throws InterruptedException {
        return waitForResult(ticketId, Void.class, waitSeconds);
    }
}
