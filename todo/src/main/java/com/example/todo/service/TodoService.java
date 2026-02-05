package com.example.todo.service;

import com.example.todo.dto.TodoForm;
import com.example.todo.entity.Todo;
import com.example.todo.repository.TodoRepository;
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

    @Transactional
    public Todo create(TodoForm form) {
        Todo todo = new Todo();
        todo.setTitle(form.getTitle());
        todo.setDescription(form.getDescription());
        todo.setPriority(form.getPriority());
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

    public org.springframework.data.domain.Page<Todo> findPage(String keyword, org.springframework.data.domain.Pageable pageable) {
        if (keyword != null && !keyword.isBlank()) {
            return todoRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        }
        return todoRepository.findAll(pageable);
    }

    @Transactional
    public int createSamples(int count) {
        int created = 0;
        com.example.todo.enums.Priority[] values = com.example.todo.enums.Priority.values();
        for (int i = 1; i <= count; i++) {
            Todo todo = new Todo();
            todo.setTitle("サンプルToDo " + i);
            todo.setDescription("ページネーション確認用のサンプルデータ " + i);
            todo.setPriority(values[i % values.length]);
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

    @Transactional
    public void delete(Long id) {
        if (!todoRepository.existsById(id)) {
            throw new IllegalArgumentException("指定されたToDoが見つかりません: " + id);
        }
        todoRepository.deleteById(id);
    }

    @Transactional
    public Todo update(Long id, String title, String description, com.example.todo.enums.Priority priority) {
        Todo todo = findById(id);
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setPriority(priority);
        return todoRepository.save(todo);
    }

    @Transactional
    public Todo toggleCompleted(Long id) {
        Todo todo = findById(id);
        todo.setCompleted(!todo.getCompleted());
        return todoRepository.save(todo);
    }
}
