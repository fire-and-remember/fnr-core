package io.fnr.core.store;

import io.fnr.core.domain.TaskRecord;
import io.fnr.core.domain.TaskStatus;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

/**
 * In-memory {@link RememberStore} implementation for local development and testing.
 *
 * <p><strong>Not for production use.</strong> All task records are stored in the JVM heap
 * and are lost on every application restart — which defeats the core purpose of this library.
 * A {@code WARNING} log is printed at startup as a reminder.
 *
 * <pre>{@code
 * @Bean
 * public RememberStore rememberStore() {
 *     return new InMemoryRememberStore();
 * }
 * }</pre>
 */
public class InMemoryRememberStore implements RememberStore {

    private static final Logger log = Logger.getLogger(InMemoryRememberStore.class.getName());

    private final ConcurrentHashMap<String, TaskRecord> store = new ConcurrentHashMap<>();

    /**
     * Creates a new in-memory store and logs a warning about its limited durability.
     */
    public InMemoryRememberStore() {
        log.warning("InMemoryRememberStore is for local/testing use only. All data will be lost on restart.");
    }

    @Override
    public void save(TaskRecord record) {
        store.put(record.getTicketId(), record);
    }

    @Override
    public Optional<TaskRecord> findByTicketId(String ticketId) {
        return Optional.ofNullable(store.get(ticketId));
    }

    @Override
    public void updateStatus(String ticketId, TaskStatus status) {
        TaskRecord record = store.get(ticketId);
        if (record != null) record.setStatus(status);
    }

    @Override
    public void updateSuccess(String ticketId, String resultPayload) {
        TaskRecord record = store.get(ticketId);
        if (record != null) {
            record.setStatus(TaskStatus.SUCCESS);
            record.setResultPayload(resultPayload);
            record.setCompletedAt(Instant.now());
        }
    }

    @Override
    public void updateFailed(String ticketId, String errorMessage) {
        TaskRecord record = store.get(ticketId);
        if (record != null) {
            record.setStatus(TaskStatus.FAILED);
            record.setErrorMessage(errorMessage);
            record.setCompletedAt(Instant.now());
        }
    }
}
