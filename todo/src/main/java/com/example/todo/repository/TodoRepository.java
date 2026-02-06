package com.example.todo.repository;

import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
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

    List<Todo> findByCategoryId(Long categoryId, org.springframework.data.domain.Sort sort);

    List<Todo> findByTitleContainingIgnoreCaseAndCategoryId(String keyword, Long categoryId, org.springframework.data.domain.Sort sort);

    void deleteByIdIn(List<Long> ids);

    Page<Todo> findByUser(User user, Pageable pageable);

    Page<Todo> findByUserAndTitleContainingIgnoreCase(User user, String keyword, Pageable pageable);

    Page<Todo> findByUserAndCategoryId(User user, Long categoryId, Pageable pageable);

    Page<Todo> findByUserAndTitleContainingIgnoreCaseAndCategoryId(User user, String keyword, Long categoryId, Pageable pageable);

    List<Todo> findByUser(User user, org.springframework.data.domain.Sort sort);

    List<Todo> findByUserAndTitleContainingIgnoreCase(User user, String keyword, org.springframework.data.domain.Sort sort);

    List<Todo> findByUserAndCategoryId(User user, Long categoryId, org.springframework.data.domain.Sort sort);

    List<Todo> findByUserAndTitleContainingIgnoreCaseAndCategoryId(User user, String keyword, Long categoryId, org.springframework.data.domain.Sort sort);

    List<Todo> findByDueDateLessThanEqual(LocalDate date);

    List<Todo> findAllByOrderByPriorityDesc();

    long countByUser(User user);

    long countByUserAndCompletedTrue(User user);

    long countByUserAndDueDateBetween(User user, LocalDate start, LocalDate end);

    @Query("SELECT t FROM Todo t WHERE t.completed = false ORDER BY t.dueDate ASC")
    List<Todo> findUncompletedOrderByDueDate();
}
