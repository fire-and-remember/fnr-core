package io.fnr.store.redis;

import io.fnr.core.domain.TaskRecord;
import io.fnr.core.domain.TaskStatus;
import io.fnr.core.store.RememberStore;
import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;

import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class RedisRememberStore implements RememberStore {

    private static final String KEY_PREFIX = "fnr:";

    private final RedissonClient redisson;
    private final Duration ttl; // null = no TTL

    public RedisRememberStore(RedissonClient redisson, Duration ttl) {
        this.redisson = redisson;
        this.ttl = ttl;
    }

    private String key(String ticketId) { return KEY_PREFIX + ticketId; }

    @Override
    public void save(TaskRecord record) {
        RBucket<TaskRecord> bucket = redisson.getBucket(key(record.getTicketId()));
        if (ttl != null) {
            bucket.set(record, ttl);
        } else {
            bucket.set(record);
        }
    }

    @Override
    public Optional<TaskRecord> findByTicketId(String ticketId) {
        return Optional.ofNullable(redisson.<TaskRecord>getBucket(key(ticketId)).get());
    }

    // preserves remaining TTL on update
    private void update(String ticketId, java.util.function.Consumer<TaskRecord> updater) {
        RBucket<TaskRecord> bucket = redisson.getBucket(key(ticketId));
        TaskRecord record = bucket.get();
        if (record == null) return;
        updater.accept(record);
        long remainingTtlMs = bucket.remainTimeToLive(); // -1 means no TTL
        if (remainingTtlMs > 0) {
            bucket.set(record, Duration.ofMillis(remainingTtlMs));
        } else {
            bucket.set(record);
        }
    }

    @Override
    public void updateStatus(String ticketId, TaskStatus status) {
        update(ticketId, r -> r.setStatus(status));
    }

    @Override
    public void updateSuccess(String ticketId, String resultPayload) {
        update(ticketId, r -> {
            r.setStatus(TaskStatus.SUCCESS);
            r.setResultPayload(resultPayload);
            r.setCompletedAt(java.time.Instant.now());
        });
    }

    @Override
    public void updateFailed(String ticketId, String errorMessage) {
        update(ticketId, r -> {
            r.setStatus(TaskStatus.FAILED);
            r.setErrorMessage(errorMessage);
            r.setCompletedAt(java.time.Instant.now());
        });
    }
}
