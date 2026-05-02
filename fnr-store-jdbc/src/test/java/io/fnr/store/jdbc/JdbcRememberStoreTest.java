package io.fnr.store.jdbc;

import io.fnr.core.domain.TaskRecord;
import io.fnr.core.domain.TaskStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

class JdbcRememberStoreTest {

    JdbcTemplate jdbcTemplate;
    JdbcRememberStore store;

    @BeforeEach
    void setUp() {
        var ds = new DriverManagerDataSource("jdbc:h2:mem:fnr_test;DB_CLOSE_DELAY=-1", "sa", "");
        jdbcTemplate = new JdbcTemplate(ds);
        jdbcTemplate.execute(JdbcRememberStore.generateSchema(JdbcStoreConfig.defaults()));
        store = new JdbcRememberStore(jdbcTemplate, JdbcStoreConfig.defaults());
    }

    @Test
    void save_and_findByTicketId() {
        TaskRecord record = sampleRecord("ticket-1");
        store.save(record);

        Optional<TaskRecord> found = store.findByTicketId("ticket-1");
        assertThat(found).isPresent();
        assertThat(found.get().getTicketId()).isEqualTo("ticket-1");
        assertThat(found.get().getJobName()).isEqualTo("test-job");
        assertThat(found.get().getStatus()).isEqualTo(TaskStatus.PENDING);
    }

    @Test
    void findByTicketId_notFound_returnsEmpty() {
        assertThat(store.findByTicketId("no-such-id")).isEmpty();
    }

    @Test
    void updateStatus_changesStatus() {
        store.save(sampleRecord("ticket-2"));
        store.updateStatus("ticket-2", TaskStatus.RUNNING);

        assertThat(store.findByTicketId("ticket-2").get().getStatus())
            .isEqualTo(TaskStatus.RUNNING);
    }

    @Test
    void updateSuccess_storesResultAndSetsSuccess() {
        store.save(sampleRecord("ticket-3"));
        store.updateSuccess("ticket-3", "{\"value\":\"done\"}");

        TaskRecord r = store.findByTicketId("ticket-3").get();
        assertThat(r.getStatus()).isEqualTo(TaskStatus.SUCCESS);
        assertThat(r.getResultPayload()).isEqualTo("{\"value\":\"done\"}");
        assertThat(r.getCompletedAt()).isNotNull();
    }

    @Test
    void updateFailed_storesErrorAndSetsFailed() {
        store.save(sampleRecord("ticket-4"));
        store.updateFailed("ticket-4", "something went wrong");

        TaskRecord r = store.findByTicketId("ticket-4").get();
        assertThat(r.getStatus()).isEqualTo(TaskStatus.FAILED);
        assertThat(r.getErrorMessage()).isEqualTo("something went wrong");
        assertThat(r.getCompletedAt()).isNotNull();
    }

    @Test
    void customConfig_schemaValidation_tableNotFound() {
        JdbcStoreConfig cfg = JdbcStoreConfig.builder().tableName("no_such_table").build();
        assertThatThrownBy(() -> new JdbcRememberStore(jdbcTemplate, cfg))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("no_such_table");
    }

    @Test
    void customConfig_schemaValidation_columnNotFound() {
        jdbcTemplate.execute(
            "CREATE TABLE custom_tasks (ticket_id VARCHAR(36) PRIMARY KEY, job_name VARCHAR(255))"
        );
        JdbcStoreConfig cfg = JdbcStoreConfig.builder().tableName("custom_tasks").build();
        assertThatThrownBy(() -> new JdbcRememberStore(jdbcTemplate, cfg))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("not found");
    }

    private TaskRecord sampleRecord(String ticketId) {
        return TaskRecord.builder()
            .ticketId(ticketId)
            .jobName("test-job")
            .timeoutSeconds(30)
            .startedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
            .status(TaskStatus.PENDING)
            .build();
    }
}
