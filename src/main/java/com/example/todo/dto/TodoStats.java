package com.example.todo.dto;

import java.time.LocalDateTime;

public class TodoStats {
    private long total;
    private long completed;
    private long pending;
    private long dueSoon;
    private LocalDateTime generatedAt;

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getCompleted() {
        return completed;
    }

    public void setCompleted(long completed) {
        this.completed = completed;
    }

    public long getPending() {
        return pending;
    }

    public void setPending(long pending) {
        this.pending = pending;
    }

    public long getDueSoon() {
        return dueSoon;
    }

    public void setDueSoon(long dueSoon) {
        this.dueSoon = dueSoon;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(LocalDateTime generatedAt) {
        this.generatedAt = generatedAt;
    }
}
