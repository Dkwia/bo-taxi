package com.bootaxi.notification.service;

import com.bootaxi.contracts.enums.NotificationStatus;
import com.bootaxi.notification.domain.NotificationTask;
import com.bootaxi.notification.repository.NotificationTaskQueueRepository;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class NotificationWorkerManager {

    private static final Logger log = LoggerFactory.getLogger(NotificationWorkerManager.class);

    private final NotificationTaskQueueRepository queueRepository;
    private final NotificationSender notificationSender;
    private final int workerCount;
    private final long pollDelayMs;
    private final AtomicBoolean running = new AtomicBoolean(true);
    private ExecutorService executorService;

    public NotificationWorkerManager(NotificationTaskQueueRepository queueRepository,
                                     NotificationSender notificationSender,
                                     @Value("${app.worker.count:4}") int workerCount,
                                     @Value("${app.worker.poll-delay-ms:1000}") long pollDelayMs) {
        this.queueRepository = queueRepository;
        this.notificationSender = notificationSender;
        this.workerCount = workerCount;
        this.pollDelayMs = pollDelayMs;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void startWorkers() {
        AtomicInteger index = new AtomicInteger(1);
        ThreadFactory threadFactory = runnable -> new Thread(runnable, "notification-worker-" + index.getAndIncrement());
        executorService = Executors.newFixedThreadPool(workerCount, threadFactory);

        for (int i = 0; i < workerCount; i++) {
            executorService.submit(this::workerLoop);
        }
        log.info("Started {} notification workers", workerCount);
    }

    @PreDestroy
    public void shutdown() {
        running.set(false);
        if (executorService == null) {
            return;
        }
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(10, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            executorService.shutdownNow();
        }
    }

    private void workerLoop() {
        while (running.get()) {
            try {
                Optional<NotificationTask> claimedTask = queueRepository.claimNextPendingTask();
                if (claimedTask.isEmpty()) {
                    Thread.sleep(pollDelayMs);
                    continue;
                }

                processTask(claimedTask.get());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                return;
            } catch (Exception exception) {
                log.error("Unexpected worker error", exception);
            }
        }
    }

    private void processTask(NotificationTask task) throws InterruptedException {
        try {
            notificationSender.send(task);
            queueRepository.markSent(task.getId());
        } catch (Exception exception) {
            int nextAttempt = task.getAttempts() + 1;
            NotificationStatus nextStatus = nextAttempt >= 3 ? NotificationStatus.FAILED : NotificationStatus.PENDING;
            queueRepository.markFailed(task.getId(), nextAttempt, nextStatus);
            log.warn("Notification processing failed: taskId={}, attempts={}, nextStatus={}",
                    task.getId(),
                    nextAttempt,
                    nextStatus,
                    exception);
        }
    }
}
