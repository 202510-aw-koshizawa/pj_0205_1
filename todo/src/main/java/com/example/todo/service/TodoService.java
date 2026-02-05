package com.example.todo.service;

import com.example.todo.dto.TodoForm;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.FORBIDDEN;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    @Transactional
    public Todo create(TodoForm form, User user) {
        Todo todo = new Todo();
        todo.setTitle(form.getTitle());
        todo.setDescription(form.getDescription());
        todo.setPriority(form.getPriority());
        todo.setDueDate(form.getDueDate());
        todo.setCategory(categoryService.findById(form.getCategoryId()));
        todo.setUser(user);
        return todoRepository.save(todo);
    }

    public List<Todo> findAll() {
        return todoRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<Todo> findAll(String keyword, Sort sort) {
        if (keyword != null && !keyword.isBlank()) {
            return todoRepository.findByTitleContainingIgnoreCase(keyword, sort);
        }
        return todoRepository.findAll(sort);
    }

    public List<Todo> findAll(User user, String keyword, Long categoryId, Sort sort) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = categoryId != null;
        if (hasKeyword && hasCategory) {
            return todoRepository.findByUserAndTitleContainingIgnoreCaseAndCategoryId(user, keyword, categoryId, sort);
        }
        if (hasKeyword) {
            return todoRepository.findByUserAndTitleContainingIgnoreCase(user, keyword, sort);
        }
        if (hasCategory) {
            return todoRepository.findByUserAndCategoryId(user, categoryId, sort);
        }
        return todoRepository.findByUser(user, sort);
    }

    public org.springframework.data.domain.Page<Todo> findPage(User user, String keyword, Long categoryId, org.springframework.data.domain.Pageable pageable) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = categoryId != null;
        if (hasKeyword && hasCategory) {
            return todoRepository.findByUserAndTitleContainingIgnoreCaseAndCategoryId(user, keyword, categoryId, pageable);
        }
        if (hasKeyword) {
            return todoRepository.findByUserAndTitleContainingIgnoreCase(user, keyword, pageable);
        }
        if (hasCategory) {
            return todoRepository.findByUserAndCategoryId(user, categoryId, pageable);
        }
        return todoRepository.findByUser(user, pageable);
    }

    @Transactional
    public int createSamples(int count, User user) {
        int created = 0;
        com.example.todo.enums.Priority[] values = com.example.todo.enums.Priority.values();
        List<com.example.todo.entity.Category> categories = categoryService.findAll();
        for (int i = 1; i <= count; i++) {
            Todo todo = new Todo();
            todo.setTitle("サンプルToDo " + i);
            todo.setDescription("ページネーション確認用のサンプルデータ " + i);
            todo.setPriority(values[i % values.length]);
            java.time.LocalDate today = java.time.LocalDate.now();
            todo.setDueDate(today.plusDays((i % 11) - 5));
            java.time.LocalDateTime createdAt = java.time.LocalDateTime.now().minusDays(i % 21);
            todo.setCreatedAt(createdAt);
            todo.setUpdatedAt(createdAt);
            if (!categories.isEmpty()) {
                todo.setCategory(categories.get(i % categories.size()));
            }
            todo.setUser(user);
            todoRepository.save(todo);
            created++;
        }
        return created;
    }

    public List<Todo> searchByTitle(String keyword) {
        return todoRepository.findByTitleContainingIgnoreCase(
                keyword, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public Todo findById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "ToDoが見つかりません: " + id));
    }

    public Todo findByIdForUser(Long id, User user) {
        Todo todo = findById(id);
        if (todo.getUser() == null || !todo.getUser().getId().equals(user.getId())) {
            throw new ResponseStatusException(FORBIDDEN, "他ユーザーのToDoにはアクセスできません");
        }
        return todo;
    }

    @Transactional
    public void delete(Long id, User user) {
        Todo todo = findByIdForUser(id, user);
        todoRepository.deleteById(todo.getId());
    }

    @Transactional
    public Todo update(Long id, String title, String description, com.example.todo.enums.Priority priority, Long categoryId, java.time.LocalDate dueDate, User user) {
        Todo todo = findByIdForUser(id, user);
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setPriority(priority);
        todo.setDueDate(dueDate);
        todo.setCategory(categoryService.findById(categoryId));
        return todoRepository.save(todo);
    }

    @Transactional
    public int deleteByIds(List<Long> ids, User user) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        List<Long> ownIds = ids.stream()
                .filter(id -> {
                    try {
                        findByIdForUser(id, user);
                        return true;
                    } catch (ResponseStatusException e) {
                        return false;
                    }
                })
                .toList();
        if (ownIds.isEmpty()) {
            return 0;
        }
        todoRepository.deleteByIdIn(ownIds);
        return ownIds.size();
    }

    @Transactional
    public Todo toggleCompleted(Long id, User user) {
        Todo todo = findByIdForUser(id, user);
        todo.setCompleted(!todo.getCompleted());
        return todoRepository.save(todo);
    }

    public User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "ユーザーが見つかりません: " + username));
    }
}
