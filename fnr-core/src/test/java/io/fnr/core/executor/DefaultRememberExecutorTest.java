package io.fnr.core.executor;

import io.fnr.core.config.VirtualThreadFnrConfig;
import io.fnr.core.config.FnrConfig;
import io.fnr.core.domain.TaskRecord;
import io.fnr.core.domain.TaskStatus;
import io.fnr.core.domain.TicketResult;
import io.fnr.core.store.RememberStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.*;

class DefaultRememberExecutorTest {

    // Minimal in-memory store for testing
    static class InMemoryStore implements RememberStore {
        TaskRecord saved;

        @Override public void save(TaskRecord r)                          { saved = r; }
        @Override public Optional<TaskRecord> findByTicketId(String id)  { return Optional.ofNullable(saved); }
        @Override public void updateStatus(String id, TaskStatus s)      { if (saved != null) saved.setStatus(s); }
        @Override public void updateSuccess(String id, String payload)   { if (saved != null) { saved.setStatus(TaskStatus.SUCCESS); saved.setResultPayload(payload); } }
        @Override public void updateFailed(String id, String msg)        { if (saved != null) { saved.setStatus(TaskStatus.FAILED); saved.setErrorMessage(msg); } }
    }

    static class UnserializableResult {
        // no-arg constructor missing, Jackson can't deserialize
        public UnserializableResult(String required) {}
    }

    InMemoryStore store;
    DefaultRememberExecutor executor;

    @BeforeEach
    void setUp() {
        store = new InMemoryStore();
        FnrConfig config = VirtualThreadFnrConfig.builder().storeResult(true).build();
        executor = new DefaultRememberExecutor(store, config);
    }

    @Test
    void success_storesResultAndTransitionsToSuccess() throws Exception {
        var ticket = executor.submit("job", 5, new Object[0], String.class, () -> "hello");

        awaitCompletion();

        TicketResult<String> result = executor.getResult(ticket.getTicketId(), String.class);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.SUCCESS);
        assertThat(result.getValue()).isEqualTo("hello");
    }

    @Test
    void failure_storesErrorAndTransitionsToFailed() throws Exception {
        var ticket = executor.submit("job", 5, new Object[0], String.class,
            () -> { throw new RuntimeException("boom"); });

        awaitCompletion();

        TicketResult<String> result = executor.getResult(ticket.getTicketId(), String.class);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(result.getErrorMessage()).contains("boom");
    }

    @Test
    void timeout_transitionsToFailed() throws Exception {
        var ticket = executor.submit("job", 1, new Object[0], String.class, () -> {
            Thread.sleep(5000);
            return "never";
        });

        Thread.sleep(2500);

        TicketResult<String> result = executor.getResult(ticket.getTicketId(), String.class);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(result.getErrorMessage()).isEqualTo("Task timed out");
    }

    @Test
    void lazyDetection_transitionsToFailedWhenTimeoutElapsed() {
        store.saved = TaskRecord.builder()
            .ticketId("lazy-id")
            .jobName("job")
            .timeoutSeconds(1)
            .startedAt(Instant.now().minusSeconds(10))
            .status(TaskStatus.RUNNING)
            .build();

        TicketResult<String> result = executor.getResult("lazy-id", String.class);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(result.getErrorMessage()).isEqualTo("Task timed out");
    }

    @Test
    void storeResultFalse_resultIsNull() throws Exception {
        store = new InMemoryStore();
        FnrConfig config = VirtualThreadFnrConfig.builder().storeResult(false).build();
        executor = new DefaultRememberExecutor(store, config);

        var ticket = executor.submit("job", 5, new Object[0], String.class, () -> "ignored");
        awaitCompletion();

        TicketResult<String> result = executor.getResult(ticket.getTicketId(), String.class);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.SUCCESS);
        assertThat(result.getValue()).isNull();
    }

    @Test
    void storeParameters_serializesParamsToRecord() throws Exception {
        store = new InMemoryStore();
        FnrConfig config = VirtualThreadFnrConfig.builder().storeParameters(true).build();
        executor = new DefaultRememberExecutor(store, config);

        executor.submit("job", 5, new Object[]{"param1", 42}, String.class, () -> "ok");

        assertThat(store.saved.getParamPayload())
            .isNotNull()
            .contains("param1")
            .contains("42");
    }

    @Test
    void getResult_withMalformedPayload_throwsIllegalArgumentException() {
        store.saved = TaskRecord.builder()
            .ticketId("bad-payload")
            .jobName("job")
            .timeoutSeconds(30)
            .startedAt(Instant.now())
            .status(TaskStatus.SUCCESS)
            .build();
        store.saved.setResultPayload("not-valid-json{{{{");

        assertThatThrownBy(() -> executor.getResult("bad-payload", String.class))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Deserialization failed");
    }

    @Test
    void unserializableParam_throwsImmediately() {
        store = new InMemoryStore();
        FnrConfig config = VirtualThreadFnrConfig.builder().storeParameters(true).build();
        executor = new DefaultRememberExecutor(store, config);

        Object unserializable = new Object() {
            // anonymous class — Jackson can't serialize
        };

        assertThatThrownBy(() ->
            executor.submit("job", 5, new Object[]{unserializable}, String.class, () -> "ok")
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessageContaining("Failed to serialize parameters");
    }

    private static final int AWAIT_POLL_INTERVAL_MS = 100;
    private static final int AWAIT_MAX_ATTEMPTS     = 50;

    @Test
    void waitForResult_blocksUntilSuccess() throws Exception {
        var ticket = executor.submit("job", 5, new Object[0], String.class, () -> {
            Thread.sleep(300);
            return "done";
        });

        TicketResult<String> result = executor.waitForResult(ticket.getTicketId(), String.class, 5);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.SUCCESS);
        assertThat(result.getValue()).isEqualTo("done");
    }

    @Test
    void waitForResult_returnsImmediatelyIfAlreadyCompleted() throws Exception {
        var ticket = executor.submit("job", 5, new Object[0], String.class, () -> "instant");
        awaitCompletion();

        long start = System.currentTimeMillis();
        TicketResult<String> result = executor.waitForResult(ticket.getTicketId(), String.class, 10);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(result.getStatus()).isEqualTo(TaskStatus.SUCCESS);
        assertThat(elapsed).isLessThan(RememberExecutor.WAIT_POLL_INTERVAL_MS * 2);
    }

    @Test
    void waitForResult_returnsCurrentStateOnTimeout() throws Exception {
        store.saved = TaskRecord.builder()
            .ticketId("stuck-id")
            .jobName("job")
            .timeoutSeconds(60)
            .startedAt(Instant.now())
            .status(TaskStatus.RUNNING)
            .build();

        TicketResult<String> result = executor.waitForResult("stuck-id", String.class, 1);
        assertThat(result.getStatus()).isEqualTo(TaskStatus.RUNNING);
    }

    private void awaitCompletion() throws InterruptedException {
        for (int i = 0; i < AWAIT_MAX_ATTEMPTS; i++) {
            if (store.saved != null &&
                (store.saved.getStatus() == TaskStatus.SUCCESS ||
                 store.saved.getStatus() == TaskStatus.FAILED)) return;
            Thread.sleep(AWAIT_POLL_INTERVAL_MS);
        }
    }
}
