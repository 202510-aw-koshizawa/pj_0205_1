package com.example.todo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    @Async("emailExecutor")
    public void sendTodoCreatedEmailAsync(String username, String title) {
        logger.info("Email send start: user={}, title={}, thread={}",
                username, title, Thread.currentThread().getName());
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.warn("Email send interrupted");
            return;
        }
        logger.info("Email send done: user={}, title={}", username, title);
    }
}
