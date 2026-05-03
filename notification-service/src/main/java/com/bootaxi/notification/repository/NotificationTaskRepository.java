package com.bootaxi.notification.repository;

import com.bootaxi.notification.domain.NotificationTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationTaskRepository extends JpaRepository<NotificationTask, Long> {

    List<NotificationTask> findByTripIdOrderByCreatedAtAsc(Long tripId);
}
