package com.example.todo.repository;

import com.example.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByCompleted(Boolean completed);

    List<Todo> findByTitleContaining(String keyword);

    List<Todo> findByDueDateLessThanEqual(LocalDate date);

    List<Todo> findAllByOrderByPriorityDesc();

    @Query("SELECT t FROM Todo t WHERE t.completed = false ORDER BY t.dueDate ASC")
    List<Todo> findUncompletedOrderByDueDate();
}
