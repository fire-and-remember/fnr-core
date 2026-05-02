package io.fnr.spring.aspect;

import io.fnr.core.config.VirtualThreadFnrConfig;
import io.fnr.core.domain.TaskRecord;
import io.fnr.core.domain.TaskStatus;
import io.fnr.core.domain.Ticket;
import io.fnr.core.executor.DefaultRememberExecutor;
import io.fnr.core.store.RememberStore;
import io.fnr.spring.annotation.Remember;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.*;

class RememberAspectTest {

    static class InMemoryStore implements RememberStore {
        TaskRecord saved;
        @Override public void save(TaskRecord r)                         { saved = r; }
        @Override public Optional<TaskRecord> findByTicketId(String id) { return Optional.ofNullable(saved); }
        @Override public void updateStatus(String id, TaskStatus s)     { if (saved != null) saved.setStatus(s); }
        @Override public void updateSuccess(String id, String p)        { if (saved != null) { saved.setStatus(TaskStatus.SUCCESS); saved.setResultPayload(p); } }
        @Override public void updateFailed(String id, String m)         { if (saved != null) { saved.setStatus(TaskStatus.FAILED); saved.setErrorMessage(m); } }
    }

    static class SampleService {
        @Remember(jobName = "send-email", timeout = 5, timeoutUnit = TimeUnit.SECONDS)
        public Ticket<String> sendEmail(String to) {
            return null; // intercepted by AOP
        }

        public String nonTicketReturn() {
            return "oops";
        }
    }

    SampleService proxied;
    InMemoryStore store;

    @BeforeEach
    void setUp() {
        store = new InMemoryStore();
        var config = VirtualThreadFnrConfig.builder().storeResult(true).build();
        var executor = new DefaultRememberExecutor(store, config);
        var aspect = new RememberAspect(executor);

        AspectJProxyFactory factory = new AspectJProxyFactory(new SampleService());
        factory.addAspect(aspect);
        proxied = factory.getProxy();
    }

    @Test
    void remember_interceptsAndReturnsTicket() {
        Ticket<String> ticket = proxied.sendEmail("user@example.com");

        assertThat(ticket).isNotNull();
        assertThat(ticket.getTicketId()).isNotBlank();
        assertThat(ticket.getJobName()).isEqualTo("send-email");
    }

    @Test
    void remember_savesRecordWithPendingStatus() {
        proxied.sendEmail("user@example.com");

        assertThat(store.saved).isNotNull();
        assertThat(store.saved.getJobName()).isEqualTo("send-email");
    }

    @Test
    void nonTicketReturnType_throwsIllegalStateException() {
        var badService = new Object() {
            @Remember(jobName = "bad")
            public String badMethod() { return "nope"; }
        };

        var executor = new DefaultRememberExecutor(store,
            VirtualThreadFnrConfig.builder().build());
        var aspect = new RememberAspect(executor);

        AspectJProxyFactory factory = new AspectJProxyFactory(badService);
        factory.addAspect(aspect);
        var badProxy = factory.getProxy();

        assertThatThrownBy(() -> {
            badProxy.getClass().getMethod("badMethod").invoke(badProxy);
        }).getCause()
          .isInstanceOf(IllegalStateException.class)
          .hasMessageContaining("must return Ticket<?>");
    }
}
