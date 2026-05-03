package io.fnr.core.store;

import io.fnr.core.domain.TaskRecord;
import io.fnr.core.domain.TaskStatus;

import java.util.Optional;

/**
 * SPI (Service Provider Interface) for persisting and retrieving task records.
 *
 * <p>FireAndRemember ships with built-in implementations for Redis, JDBC, and MongoDB.
 * Implement this interface to integrate with a custom storage backend.
 *
 * <p>Implementations must be thread-safe — multiple threads may call these methods
 * concurrently for different task IDs.
 *
 * <pre>{@code
 * @Bean
 * public RememberStore rememberStore() {
 *     return new MyCustomStore(...);
 * }
 * }</pre>
 */
public interface RememberStore {

    /**
     * Persists a new task record.
     *
     * <p>Called once when the task is submitted, before execution begins.
     * The initial status is always {@link TaskStatus#PENDING}.
     *
     * @param record the task record to save
     */
    void save(TaskRecord record);

    /**
     * Retrieves a task record by its ticket ID.
     *
     * @param ticketId the unique ticket ID
     * @return the task record, or {@link Optional#empty()} if not found
     */
    Optional<TaskRecord> findByTicketId(String ticketId);

    /**
     * Updates the status of an existing task record.
     *
     * <p>Called when the task transitions to {@link TaskStatus#RUNNING}.
     *
     * @param ticketId the unique ticket ID
     * @param status   the new status to set
     */
    void updateStatus(String ticketId, TaskStatus status);

    /**
     * Marks a task as successfully completed and stores the result payload.
     *
     * <p>Sets status to {@link TaskStatus#SUCCESS}, stores the JSON-serialized result,
     * and records the completion timestamp.
     *
     * @param ticketId      the unique ticket ID
     * @param resultPayload JSON-serialized result value, or {@code null} if result storage is disabled
     */
    void updateSuccess(String ticketId, String resultPayload);

    /**
     * Marks a task as failed and stores the error message.
     *
     * <p>Sets status to {@link TaskStatus#FAILED}, stores the error message,
     * and records the completion timestamp.
     *
     * @param ticketId     the unique ticket ID
     * @param errorMessage a description of the failure reason
     */
    void updateFailed(String ticketId, String errorMessage);
}
