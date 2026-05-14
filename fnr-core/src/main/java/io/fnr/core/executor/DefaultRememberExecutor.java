package io.fnr.core.executor;

import io.fnr.core.config.FnrConfig;
import io.fnr.core.config.ThreadPoolFnrConfig;
import io.fnr.core.domain.*;
import io.fnr.core.exception.TicketNotFoundException;
import io.fnr.core.store.RememberStore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.logging.Logger;

public class DefaultRememberExecutor implements RememberExecutor {

    private static final Logger log = Logger.getLogger(DefaultRememberExecutor.class.getName());

    private final RememberStore store;
    private final FnrConfig config;
    private final ExecutorService executorService;
    private final ObjectMapper objectMapper;

    public DefaultRememberExecutor(RememberStore store, FnrConfig config) {
        this.store = store;
        this.config = config;
        this.executorService = (config instanceof ThreadPoolFnrConfig tpConfig)
            ? Executors.newFixedThreadPool(tpConfig.getThreadPoolSize())
            : Executors.newVirtualThreadPerTaskExecutor();
        this.objectMapper = createObjectMapperIfAvailable();
    }

    private static ObjectMapper createObjectMapperIfAvailable() {
        try {
            return new ObjectMapper();
        } catch (NoClassDefFoundError e) {
            return null;
        }
    }

    @Override
    public <T> Ticket<T> submit(String jobName, long timeoutSeconds, Object[] params, Class<T> resultType,
                                boolean storeResult, boolean storeParameters, Callable<T> task) {
        if (jobName == null || jobName.isBlank()) throw new IllegalArgumentException("jobName must not be blank");
        if (timeoutSeconds <= 0) throw new IllegalArgumentException("timeoutSeconds must be greater than 0");
        if (task == null) throw new IllegalArgumentException("task must not be null");
        if ((storeResult || storeParameters) && objectMapper == null) {
            throw new IllegalStateException(
                "jackson-databind is required when storeResult or storeParameters is enabled. "
                + "Add 'com.fasterxml.jackson.core:jackson-databind' to your dependencies."
            );
        }

        String paramPayload = serializeParams(params, storeParameters);
        String ticketId = UUID.randomUUID().toString();

        TaskRecord record = TaskRecord.builder()
            .ticketId(ticketId)
            .jobName(jobName)
            .timeoutSeconds(timeoutSeconds)
            .startedAt(Instant.now())
            .status(TaskStatus.PENDING)
            .paramPayload(paramPayload)
            .build();

        store.save(record);

        executorService.submit(() -> executeWithTracking(ticketId, timeoutSeconds, storeResult, task));

        return new Ticket<>(ticketId, jobName);
    }

    private <T> void executeWithTracking(String ticketId, long timeoutSeconds, boolean storeResult, Callable<T> task) {
        store.updateStatus(ticketId, TaskStatus.RUNNING);
        Future<T> future = executorService.submit(task);
        try {
            T result = future.get(timeoutSeconds, TimeUnit.SECONDS);
            String payload = storeResult ? objectMapper.writeValueAsString(result) : null;
            store.updateSuccess(ticketId, payload);
        } catch (TimeoutException e) {
            future.cancel(true);
            store.updateFailed(ticketId, "Task timed out");
        } catch (Exception e) {
            log.warning("Task failed [ticketId=" + ticketId + "]: " + e.getMessage());
            store.updateFailed(ticketId, "Task execution failed");
        }
    }

    @Override
    public <T> TicketResult<T> getResult(String ticketId, Class<T> resultType) {
        TaskRecord record = store.findByTicketId(ticketId)
            .orElseThrow(() -> new TicketNotFoundException(ticketId));

        String params = record.getParamPayload();

        if (record.getStatus() == TaskStatus.RUNNING) {
            Instant deadline = record.getStartedAt().plusSeconds(record.getTimeoutSeconds());
            if (Instant.now().isAfter(deadline)) {
                store.updateFailed(ticketId, "Task timed out");
                return TicketResult.<T>failed("Task timed out").withParamPayload(params);
            }
        }

        return switch (record.getStatus()) {
            case PENDING -> TicketResult.<T>pending().withParamPayload(params);
            case RUNNING -> TicketResult.<T>running().withParamPayload(params);
            case FAILED  -> TicketResult.<T>failed(record.getErrorMessage()).withParamPayload(params);
            case SUCCESS -> {
                if (record.getResultPayload() != null && resultType != Void.class) {
                    try {
                        yield TicketResult.success(objectMapper.readValue(record.getResultPayload(), resultType))
                            .withParamPayload(params);
                    } catch (JsonProcessingException e) {
                        throw new IllegalArgumentException("Deserialization failed: " + e.getMessage(), e);
                    }
                }
                yield TicketResult.<T>success(null).withParamPayload(params);
            }
        };
    }

    private String serializeParams(Object[] params, boolean storeParameters) {
        if (!storeParameters || params == null || params.length == 0) return null;
        try {
            return objectMapper.writeValueAsString(params);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException(
                "Failed to serialize parameters: " + e.getMessage(), e
            );
        }
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
