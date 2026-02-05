package com.example.todo.repository;

import com.example.todo.entity.Todo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface TodoRepository extends JpaRepository<Todo, Long> {

    List<Todo> findByCompleted(Boolean completed);

    List<Todo> findByTitleContaining(String keyword);

    List<Todo> findByTitleContainingIgnoreCase(String keyword, org.springframework.data.domain.Sort sort);

    Page<Todo> findByTitleContainingIgnoreCase(String keyword, Pageable pageable);

    Page<Todo> findByCategoryId(Long categoryId, Pageable pageable);

    Page<Todo> findByTitleContainingIgnoreCaseAndCategoryId(String keyword, Long categoryId, Pageable pageable);

    void deleteByIdIn(List<Long> ids);

    List<Todo> findByDueDateLessThanEqual(LocalDate date);

    List<Todo> findAllByOrderByPriorityDesc();

    @Query("SELECT t FROM Todo t WHERE t.completed = false ORDER BY t.dueDate ASC")
    List<Todo> findUncompletedOrderByDueDate();
}
