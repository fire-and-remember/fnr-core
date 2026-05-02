package io.fnr.store.mongo;

import io.fnr.core.domain.TaskRecord;
import io.fnr.core.domain.TaskStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataMongoTest
@Import(MongoRememberStore.class)
@TestPropertySource(properties = "de.flapdoodle.mongodb.embedded.version=7.0.2")
class MongoRememberStoreTest {

    @Autowired
    MongoRememberStore store;

    @Autowired
    MongoTemplate mongoTemplate;

    @AfterEach
    void tearDown() {
        mongoTemplate.dropCollection(MongoRememberStore.COLLECTION);
    }

    @Test
    void save_and_findByTicketId() {
        store.save(sampleRecord("ticket-1"));

        Optional<TaskRecord> found = store.findByTicketId("ticket-1");
        assertThat(found).isPresent();
        assertThat(found.get().getTicketId()).isEqualTo("ticket-1");
        assertThat(found.get().getJobName()).isEqualTo("test-job");
        assertThat(found.get().getStatus()).isEqualTo(TaskStatus.PENDING);
        assertThat(found.get().getTimeoutSeconds()).isEqualTo(30L);
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
    void save_withParamPayload_roundtrips() {
        TaskRecord record = TaskRecord.builder()
            .ticketId("ticket-5")
            .jobName("test-job")
            .timeoutSeconds(30)
            .startedAt(Instant.now().truncatedTo(ChronoUnit.MILLIS))
            .status(TaskStatus.PENDING)
            .paramPayload("{\"key\":\"value\"}")
            .build();
        store.save(record);

        TaskRecord found = store.findByTicketId("ticket-5").get();
        assertThat(found.getParamPayload()).isEqualTo("{\"key\":\"value\"}");
    }

    @Test
    void save_withoutParamPayload_paramPayloadIsNull() {
        store.save(sampleRecord("ticket-6"));

        TaskRecord found = store.findByTicketId("ticket-6").get();
        assertThat(found.getParamPayload()).isNull();
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
