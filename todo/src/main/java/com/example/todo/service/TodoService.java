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
    public Todo update(Long id, String title, String description, Integer priority) {
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
