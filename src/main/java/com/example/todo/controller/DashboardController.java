package com.example.todo.controller;

import com.example.todo.dto.TodoStats;
import com.example.todo.entity.User;
import com.example.todo.service.ReportService;
import com.example.todo.service.TodoService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/dashboard")
public class DashboardController {

    private final TodoService todoService;
    private final ReportService reportService;

    public DashboardController(TodoService todoService, ReportService reportService) {
        this.todoService = todoService;
        this.reportService = reportService;
    }

    @GetMapping
    public TodoStats dashboard(@AuthenticationPrincipal UserDetails userDetails) {
        User user = todoService.loadUser(userDetails.getUsername());
        CompletableFuture<TodoStats> statsFuture = reportService.generateStatsAsync(user);
        CompletableFuture<Long> dueSoonFuture = reportService.countDueSoonAsync(user);

        CompletableFuture.allOf(statsFuture, dueSoonFuture).join();

        TodoStats stats = statsFuture.join();
        stats.setDueSoon(dueSoonFuture.join());
        return stats;
    }
}
