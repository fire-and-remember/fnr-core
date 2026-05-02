package io.fnr.store.mongo;

import io.fnr.core.domain.TaskRecord;
import io.fnr.core.domain.TaskStatus;
import io.fnr.core.store.RememberStore;
import org.bson.Document;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

public class MongoRememberStore implements RememberStore {

    static final String COLLECTION = "fnr_tasks";

    private final MongoTemplate mongoTemplate;

    public MongoRememberStore(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void save(TaskRecord record) {
        mongoTemplate.insert(toDocument(record), COLLECTION);
    }

    @Override
    public Optional<TaskRecord> findByTicketId(String ticketId) {
        Query query = Query.query(Criteria.where("ticketId").is(ticketId));
        Document doc = mongoTemplate.findOne(query, Document.class, COLLECTION);
        return Optional.ofNullable(doc).map(MongoRememberStore::toTaskRecord);
    }

    @Override
    public void updateStatus(String ticketId, TaskStatus status) {
        mongoTemplate.updateFirst(
            Query.query(Criteria.where("ticketId").is(ticketId)),
            Update.update("status", status.name()),
            COLLECTION
        );
    }

    @Override
    public void updateSuccess(String ticketId, String resultPayload) {
        mongoTemplate.updateFirst(
            Query.query(Criteria.where("ticketId").is(ticketId)),
            new Update()
                .set("status", TaskStatus.SUCCESS.name())
                .set("resultPayload", resultPayload)
                .set("completedAt", new Date()),
            COLLECTION
        );
    }

    @Override
    public void updateFailed(String ticketId, String errorMessage) {
        mongoTemplate.updateFirst(
            Query.query(Criteria.where("ticketId").is(ticketId)),
            new Update()
                .set("status", TaskStatus.FAILED.name())
                .set("errorMessage", errorMessage)
                .set("completedAt", new Date()),
            COLLECTION
        );
    }

    private static Document toDocument(TaskRecord record) {
        Document doc = new Document()
            .append("ticketId", record.getTicketId())
            .append("jobName", record.getJobName())
            .append("status", record.getStatus().name())
            .append("startedAt", Date.from(record.getStartedAt()))
            .append("timeoutSeconds", record.getTimeoutSeconds());
        if (record.getParamPayload() != null) doc.append("paramPayload", record.getParamPayload());
        return doc;
    }

    private static TaskRecord toTaskRecord(Document doc) {
        TaskRecord record = TaskRecord.builder()
            .ticketId(doc.getString("ticketId"))
            .jobName(doc.getString("jobName"))
            .timeoutSeconds(doc.getLong("timeoutSeconds"))
            .startedAt(doc.getDate("startedAt").toInstant())
            .status(TaskStatus.valueOf(doc.getString("status")))
            .paramPayload(doc.getString("paramPayload"))
            .build();

        Date completedAt = doc.getDate("completedAt");
        if (completedAt != null) record.setCompletedAt(completedAt.toInstant());
        record.setResultPayload(doc.getString("resultPayload"));
        record.setErrorMessage(doc.getString("errorMessage"));
        return record;
    }
}
