package io.fnr.core.service;

import io.fnr.core.domain.TicketResult;

public interface FnrTicketService {
    <T> TicketResult<T> getResult(String ticketId, Class<T> resultType);

    default TicketResult<Void> getResult(String ticketId) {
        return getResult(ticketId, Void.class);
    }

    default TicketResult<Void> waitForResult(String ticketId, long waitSeconds) throws InterruptedException {
        return waitForResult(ticketId, Void.class, waitSeconds);
    }

    long WAIT_POLL_INTERVAL_MS = 500;

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
}
