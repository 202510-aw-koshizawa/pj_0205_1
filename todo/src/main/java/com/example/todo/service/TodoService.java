package com.example.todo.service;

import com.example.todo.dto.TodoForm;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.exception.BusinessException;
import com.example.todo.exception.TodoNotFoundException;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import com.example.todo.audit.Auditable;
import com.example.todo.audit.AuditAction;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class TodoService {

    private final TodoRepository todoRepository;
    private final CategoryService categoryService;
    private final UserRepository userRepository;
    private final AuditService auditService;
    private final MailService mailService;

    @Transactional(rollbackFor = Exception.class)
    @Auditable(action = AuditAction.CREATE)
    public Todo create(TodoForm form, User user) {
        Todo todo = new Todo();
        todo.setTitle(form.getTitle());
        todo.setDescription(form.getDescription());
        todo.setPriority(form.getPriority());
        todo.setDueDate(form.getDueDate());
        todo.setCategory(categoryService.findById(form.getCategoryId()));
        todo.setUser(user);
        Todo saved = todoRepository.save(todo);
        mailService.sendTodoCreatedAsync(user, saved);
        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    @Auditable(action = AuditAction.CREATE)
    public Todo createFromApi(com.example.todo.dto.ApiTodoRequest req, User user) {
        Todo todo = new Todo();
        todo.setTitle(req.getTitle());
        todo.setDescription(req.getDescription());
        todo.setPriority(req.getPriority());
        todo.setDueDate(req.getDueDate());
        todo.setCategory(categoryService.findById(req.getCategoryId()));
        todo.setUser(user);
        if (req.getCompleted() != null) {
            todo.setCompleted(req.getCompleted());
        }
        Todo saved = todoRepository.save(todo);
        mailService.sendTodoCreatedAsync(user, saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Todo> findAll() {
        return todoRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public List<Todo> findAll(String keyword, Sort sort) {
        if (keyword != null && !keyword.isBlank()) {
            return todoRepository.findByTitleContainingIgnoreCase(keyword, sort);
        }
        return todoRepository.findAll(sort);
    }

    @Transactional(readOnly = true)
    public List<Todo> findAll(User user, String keyword, Long categoryId, Sort sort, boolean isAdmin) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = categoryId != null;
        if (isAdmin) {
            if (hasKeyword && hasCategory) {
                return todoRepository.findByTitleContainingIgnoreCaseAndCategoryId(keyword, categoryId, sort);
            }
            if (hasKeyword) {
                return todoRepository.findByTitleContainingIgnoreCase(keyword, sort);
            }
            if (hasCategory) {
                return todoRepository.findByCategoryId(categoryId, sort);
            }
            return todoRepository.findAll(sort);
        }
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

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Todo> findPage(User user, String keyword, Long categoryId, org.springframework.data.domain.Pageable pageable, boolean isAdmin) {
        boolean hasKeyword = keyword != null && !keyword.isBlank();
        boolean hasCategory = categoryId != null;
        if (isAdmin) {
            if (hasKeyword && hasCategory) {
                return todoRepository.findByTitleContainingIgnoreCaseAndCategoryId(keyword, categoryId, pageable);
            }
            if (hasKeyword) {
                return todoRepository.findByTitleContainingIgnoreCase(keyword, pageable);
            }
            if (hasCategory) {
                return todoRepository.findByCategoryId(categoryId, pageable);
            }
            return todoRepository.findAll(pageable);
        }
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

    @Transactional(rollbackFor = Exception.class)
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
            Todo saved = todoRepository.save(todo);
            auditService.log("CREATE_SAMPLE", saved.getId(), user.getUsername());
            created++;
        }
        return created;
    }

    @Transactional(readOnly = true)
    public List<Todo> searchByTitle(String keyword) {
        return todoRepository.findByTitleContainingIgnoreCase(
                keyword, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    @Transactional(readOnly = true)
    public Todo findById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Todo findByIdForUser(Long id, User user) {
        return findByIdWithAccess(id, user, false);
    }

    @Transactional(readOnly = true)
    public Todo findByIdWithAccess(Long id, User user, boolean isAdmin) {
        Todo todo = findById(id);
        if (!isAdmin) {
            if (todo.getUser() == null || !todo.getUser().getId().equals(user.getId())) {
                throw new BusinessException("E403", "他ユーザーのToDoにはアクセスできません");
            }
        }
        return todo;
    }

    @Transactional(rollbackFor = Exception.class)
    @Auditable(action = AuditAction.DELETE)
    public void delete(Long id, User user, boolean isAdmin) {
        Todo todo = findByIdWithAccess(id, user, isAdmin);
        todoRepository.deleteById(todo.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    @Auditable(action = AuditAction.UPDATE)
    public Todo update(Long id, String title, String description, com.example.todo.enums.Priority priority, Long categoryId, java.time.LocalDate dueDate, User user, boolean isAdmin) {
        Todo todo = findByIdWithAccess(id, user, isAdmin);
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setPriority(priority);
        todo.setDueDate(dueDate);
        todo.setCategory(categoryService.findById(categoryId));
        Todo saved = todoRepository.save(todo);
        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    @Auditable(action = AuditAction.UPDATE)
    public Todo updateFromApi(Long id, com.example.todo.dto.ApiTodoRequest req, User user, boolean isAdmin) {
        Todo todo = findByIdWithAccess(id, user, isAdmin);
        todo.setTitle(req.getTitle());
        todo.setDescription(req.getDescription());
        todo.setPriority(req.getPriority());
        todo.setDueDate(req.getDueDate());
        todo.setCategory(categoryService.findById(req.getCategoryId()));
        if (req.getCompleted() != null) {
            todo.setCompleted(req.getCompleted());
        }
        Todo saved = todoRepository.save(todo);
        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    public int deleteByIds(List<Long> ids, User user, boolean isAdmin) {
        if (ids == null || ids.isEmpty()) {
            return 0;
        }
        if (isAdmin) {
            todoRepository.deleteByIdIn(ids);
            ids.forEach(id -> auditService.log("DELETE", id, user.getUsername()));
            return ids.size();
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
        ownIds.forEach(id -> auditService.log("DELETE", id, user.getUsername()));
        return ownIds.size();
    }

    @Transactional(rollbackFor = Exception.class)
    @Auditable(action = AuditAction.TOGGLE)
    public Todo toggleCompleted(Long id, User user, boolean isAdmin) {
        Todo todo = findByIdWithAccess(id, user, isAdmin);
        todo.setCompleted(!todo.getCompleted());
        Todo saved = todoRepository.save(todo);
        return saved;
    }

    @Transactional(readOnly = true)
    public User loadUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("E401", "ユーザーが見つかりません: " + username));
    }
}
