package com.example.todo.service;

import com.example.todo.dto.TodoStats;
import com.example.todo.entity.User;
import com.example.todo.repository.TodoRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

@Service
public class ReportService {

    private final TodoRepository todoRepository;

    public ReportService(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    @Async
    public CompletableFuture<TodoStats> generateStatsAsync(User user) {
        TodoStats stats = new TodoStats();
        long total = todoRepository.countByUser(user);
        long completed = todoRepository.countByUserAndCompletedTrue(user);
        stats.setTotal(total);
        stats.setCompleted(completed);
        stats.setPending(Math.max(0, total - completed));
        stats.setGeneratedAt(LocalDateTime.now());
        return CompletableFuture.completedFuture(stats);
    }

    @Async
    public CompletableFuture<Long> countDueSoonAsync(User user) {
        LocalDate today = LocalDate.now();
        LocalDate end = today.plusDays(3);
        long count = todoRepository.countByUserAndDueDateBetween(user, today, end);
        return CompletableFuture.completedFuture(count);
    }
}
