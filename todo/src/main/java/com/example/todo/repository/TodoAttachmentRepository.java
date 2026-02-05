package com.example.todo.repository;

import com.example.todo.entity.Todo;
import com.example.todo.entity.TodoAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TodoAttachmentRepository extends JpaRepository<TodoAttachment, Long> {
    List<TodoAttachment> findByTodoOrderByUploadedAtDesc(Todo todo);
}
