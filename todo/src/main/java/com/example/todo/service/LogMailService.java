package com.example.todo.service;

import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class LogMailService implements MailService {

    private static final Logger logger = LoggerFactory.getLogger(LogMailService.class);

    @Override
    @Async("emailExecutor")
    public void sendTodoCreatedAsync(User user, Todo todo) {
        logger.info("Mail[log-only] create: to={}, title={}, thread={}",
                user.getUsername(), todo.getTitle(), Thread.currentThread().getName());
    }

    @Override
    @Async("emailExecutor")
    public void sendDeadlineReminderAsync(User user, Todo todo) {
        logger.info("Mail[log-only] reminder: to={}, title={}, dueDate={}, thread={}",
                user.getUsername(),
                todo.getTitle(),
                todo.getDueDate(),
                Thread.currentThread().getName());
    }
}
