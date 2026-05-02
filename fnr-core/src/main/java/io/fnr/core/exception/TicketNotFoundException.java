package io.fnr.core.exception;

/**
 * Thrown when no task record exists for the given ticket ID.
 *
 * <p>This can happen if:
 * <ul>
 *   <li>The ticket ID is wrong or was never issued.</li>
 *   <li>The record expired (e.g., TTL elapsed in Redis) or was manually deleted.</li>
 * </ul>
 */
public class TicketNotFoundException extends RuntimeException {

    /**
     * @param ticketId the ticket ID that was not found in the store
     */
    public TicketNotFoundException(String ticketId) {
        super("Ticket not found: " + ticketId);
    }
}
