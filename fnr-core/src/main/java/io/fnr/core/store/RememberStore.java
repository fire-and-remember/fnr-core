package io.fnr.core.store;

import io.fnr.core.domain.TaskRecord;
import io.fnr.core.domain.TaskStatus;

import java.util.Optional;

public interface RememberStore {
    void save(TaskRecord record);
    Optional<TaskRecord> findByTicketId(String ticketId);
    void updateStatus(String ticketId, TaskStatus status);
    void updateSuccess(String ticketId, String resultPayload);
    void updateFailed(String ticketId, String errorMessage);
}
