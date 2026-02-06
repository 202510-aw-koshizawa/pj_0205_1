package com.example.todo.service;

import com.example.todo.entity.Todo;
import com.example.todo.entity.User;

public interface MailService {
    void sendTodoCreatedAsync(User user, Todo todo);

    void sendDeadlineReminderAsync(User user, Todo todo);
}
