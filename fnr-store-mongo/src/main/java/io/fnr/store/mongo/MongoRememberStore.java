package io.fnr.store.mongo;

import io.fnr.core.domain.TaskRecord;
import io.fnr.core.domain.TaskStatus;
import io.fnr.core.store.RememberStore;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import java.time.Instant;
import java.util.Optional;

public class MongoRememberStore implements RememberStore {

    private static final String COLLECTION = "fnr_tasks";

    private final MongoTemplate mongoTemplate;

    public MongoRememberStore(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void save(TaskRecord record) {
        mongoTemplate.save(record, COLLECTION);
    }

    @Override
    public Optional<TaskRecord> findByTicketId(String ticketId) {
        Query query = Query.query(Criteria.where("ticketId").is(ticketId));
        return Optional.ofNullable(mongoTemplate.findOne(query, TaskRecord.class, COLLECTION));
    }

    @Override
    public void updateStatus(String ticketId, TaskStatus status) {
        Query query = Query.query(Criteria.where("ticketId").is(ticketId));
        Update update = Update.update("status", status);
        mongoTemplate.updateFirst(query, update, COLLECTION);
    }

    @Override
    public void updateSuccess(String ticketId, String resultPayload) {
        Query query = Query.query(Criteria.where("ticketId").is(ticketId));
        Update update = new Update()
            .set("status", TaskStatus.SUCCESS)
            .set("resultPayload", resultPayload)
            .set("completedAt", Instant.now());
        mongoTemplate.updateFirst(query, update, COLLECTION);
    }

    @Override
    public void updateFailed(String ticketId, String errorMessage) {
        Query query = Query.query(Criteria.where("ticketId").is(ticketId));
        Update update = new Update()
            .set("status", TaskStatus.FAILED)
            .set("errorMessage", errorMessage)
            .set("completedAt", Instant.now());
        mongoTemplate.updateFirst(query, update, COLLECTION);
    }
}
