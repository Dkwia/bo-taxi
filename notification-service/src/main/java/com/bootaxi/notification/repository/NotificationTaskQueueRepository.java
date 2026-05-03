package com.bootaxi.notification.repository;

import com.bootaxi.contracts.enums.NotificationStatus;
import com.bootaxi.contracts.enums.RecipientType;
import com.bootaxi.notification.domain.NotificationTask;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Repository
public class NotificationTaskQueueRepository {

    private static final String CLAIM_SQL = """
            WITH candidate AS (
                SELECT id
                FROM notification_tasks
                WHERE status = 'PENDING'
                ORDER BY created_at
                LIMIT 1
                FOR UPDATE SKIP LOCKED
            )
            UPDATE notification_tasks nt
            SET status = 'PROCESSING', updated_at = NOW()
            FROM candidate
            WHERE nt.id = candidate.id
            RETURNING nt.id, nt.trip_id, nt.recipient_type, nt.recipient_id, nt.message,
                      nt.status, nt.attempts, nt.created_at, nt.updated_at
            """;

    private final JdbcTemplate jdbcTemplate;

    public NotificationTaskQueueRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public Optional<NotificationTask> claimNextPendingTask() {
        List<NotificationTask> tasks = jdbcTemplate.query(CLAIM_SQL, this::mapRow);
        if (tasks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(tasks.getFirst());
    }

    public void markSent(Long taskId) {
        jdbcTemplate.update(
                "UPDATE notification_tasks SET status = ?, updated_at = NOW() WHERE id = ?",
                NotificationStatus.SENT.name(),
                taskId
        );
    }

    public void markFailed(Long taskId, int attempts, NotificationStatus nextStatus) {
        jdbcTemplate.update(
                "UPDATE notification_tasks SET attempts = ?, status = ?, updated_at = NOW() WHERE id = ?",
                attempts,
                nextStatus.name(),
                taskId
        );
    }

    private NotificationTask mapRow(ResultSet rs, int rowNum) throws SQLException {
        NotificationTask task = new NotificationTask();
        task.setId(rs.getLong("id"));
        task.setTripId(rs.getLong("trip_id"));
        task.setRecipientType(RecipientType.valueOf(rs.getString("recipient_type")));
        task.setRecipientId(rs.getLong("recipient_id"));
        task.setMessage(rs.getString("message"));
        task.setStatus(NotificationStatus.valueOf(rs.getString("status")));
        task.setAttempts(rs.getInt("attempts"));
        task.setCreatedAt(rs.getTimestamp("created_at").toInstant());
        task.setUpdatedAt(rs.getTimestamp("updated_at").toInstant());
        return task;
    }
}
