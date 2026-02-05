package com.example.todo.dto;

import com.example.todo.entity.Todo;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class TodoDto {
    private Long id;
    private String title;
    private String description;
    private String priority;
    private Boolean completed;
    private Long categoryId;
    private String categoryName;
    private LocalDate dueDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String username;

    public static TodoDto from(Todo todo) {
        TodoDto dto = new TodoDto();
        dto.id = todo.getId();
        dto.title = todo.getTitle();
        dto.description = todo.getDescription();
        dto.priority = todo.getPriority() != null ? todo.getPriority().name() : null;
        dto.completed = todo.getCompleted();
        dto.categoryId = todo.getCategory() != null ? todo.getCategory().getId() : null;
        dto.categoryName = todo.getCategory() != null ? todo.getCategory().getName() : null;
        dto.dueDate = todo.getDueDate();
        dto.createdAt = todo.getCreatedAt();
        dto.updatedAt = todo.getUpdatedAt();
        dto.username = todo.getUser() != null ? todo.getUser().getUsername() : null;
        return dto;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getPriority() {
        return priority;
    }

    public Boolean getCompleted() {
        return completed;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public String getUsername() {
        return username;
    }
}
