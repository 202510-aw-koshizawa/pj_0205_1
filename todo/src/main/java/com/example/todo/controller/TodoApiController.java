package com.example.todo.controller;

import com.example.todo.dto.ApiResponse;
import com.example.todo.dto.ApiTodoRequest;
import com.example.todo.dto.TodoDto;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@CrossOrigin
@RequiredArgsConstructor
public class TodoApiController {

    private final TodoService todoService;

    @GetMapping
    public ApiResponse<List<TodoDto>> getAll(@RequestParam(required = false) String keyword,
                                             @RequestParam(required = false) Long categoryId,
                                             @RequestParam(required = false, defaultValue = "createdAt") String sort,
                                             @RequestParam(required = false, defaultValue = "desc") String order,
                                             @AuthenticationPrincipal UserDetails userDetails) {
        org.springframework.data.domain.Sort.Direction direction =
                "asc".equalsIgnoreCase(order) ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC;
        String sortKey = normalizeSort(sort);
        org.springframework.data.domain.Sort sortSpec = buildSort(sortKey, direction);

        User user = todoService.loadUser(userDetails.getUsername());
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");

        List<Todo> todos = todoService.findAll(user, keyword, categoryId, sortSpec, isAdmin);
        List<TodoDto> data = todos.stream().map(TodoDto::from).toList();
        return ApiResponse.success(data);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoDto>> getById(@PathVariable Long id,
                                                        @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = todoService.loadUser(userDetails.getUsername());
            boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
            Todo todo = todoService.findByIdWithAccess(id, user, isAdmin);
            return ResponseEntity.ok(ApiResponse.success(TodoDto.from(todo)));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(e.getReason()));
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TodoDto>> create(@Valid @RequestBody ApiTodoRequest req,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        User user = todoService.loadUser(userDetails.getUsername());
        Todo created = todoService.createFromApi(req, user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(TodoDto.from(created)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TodoDto>> update(@PathVariable Long id,
                                                       @Valid @RequestBody ApiTodoRequest req,
                                                       @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = todoService.loadUser(userDetails.getUsername());
            boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
            Todo updated = todoService.updateFromApi(id, req, user, isAdmin);
            return ResponseEntity.ok(ApiResponse.success(TodoDto.from(updated)));
        } catch (org.springframework.web.server.ResponseStatusException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body(ApiResponse.error(e.getReason()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id,
                                       @AuthenticationPrincipal UserDetails userDetails) {
        User user = todoService.loadUser(userDetails.getUsername());
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
        todoService.delete(id, user, isAdmin);
        return ResponseEntity.noContent().build();
    }

    private boolean hasRole(UserDetails userDetails, String role) {
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            if (authority.getAuthority().equals(role)) {
                return true;
            }
        }
        return false;
    }

    private String normalizeSort(String sort) {
        if (sort == null) return "createdAt";
        return switch (sort) {
            case "id", "title", "createdAt", "completed", "priority", "dueDate" -> sort;
            default -> "createdAt";
        };
    }

    private org.springframework.data.domain.Sort buildSort(String sortKey,
                                                          org.springframework.data.domain.Sort.Direction direction) {
        String sortColumn = "priority".equals(sortKey) ? "priorityRank" : sortKey;
        return org.springframework.data.domain.Sort.by(direction, sortColumn);
    }
}
