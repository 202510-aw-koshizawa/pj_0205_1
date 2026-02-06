package com.example.todo.service;

import com.example.todo.dto.TodoForm;
import com.example.todo.entity.Category;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.enums.Priority;
import com.example.todo.exception.BusinessException;
import com.example.todo.repository.TodoRepository;
import com.example.todo.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private CategoryService categoryService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditService auditService;

    @InjectMocks
    private TodoService todoService;

    @Test
    @DisplayName("create: フォーム内容が保存され監査ログが記録される")
    void create_savesTodo_and_logsAudit() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user");

        Category category = new Category();
        category.setId(10L);
        category.setName("仕事");

        TodoForm form = new TodoForm("title", "desc", Priority.MEDIUM, category.getId(), LocalDate.now());

        when(categoryService.findById(category.getId())).thenReturn(category);
        when(todoRepository.save(any(Todo.class))).thenAnswer(inv -> {
            Todo t = inv.getArgument(0);
            t.setId(100L);
            return t;
        });

        Todo saved = todoService.create(form, user);

        assertThat(saved.getId()).isEqualTo(100L);
        assertThat(saved.getTitle()).isEqualTo("title");
        assertThat(saved.getDescription()).isEqualTo("desc");
        assertThat(saved.getPriority()).isEqualTo(Priority.MEDIUM);
        assertThat(saved.getCategory()).isEqualTo(category);
        assertThat(saved.getUser()).isEqualTo(user);
        verify(auditService).log(eq("CREATE"), eq(100L), eq("user"));
    }

    @Test
    @DisplayName("findByIdWithAccess: 他ユーザーのToDoは拒否される")
    void findByIdWithAccess_forbidden_when_not_owner() {
        User owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");

        User other = new User();
        other.setId(2L);
        other.setUsername("other");

        Todo todo = new Todo();
        todo.setId(10L);
        todo.setUser(owner);

        when(todoRepository.findById(10L)).thenReturn(Optional.of(todo));

        assertThatThrownBy(() -> todoService.findByIdWithAccess(10L, other, false))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("他ユーザーのToDoにはアクセスできません");
    }

    @Test
    @DisplayName("deleteByIds: 管理者は一括削除できる")
    void deleteByIds_admin_deletesAll() {
        User admin = new User();
        admin.setId(99L);
        admin.setUsername("admin");

        List<Long> ids = List.of(1L, 2L, 3L);

        int deleted = todoService.deleteByIds(ids, admin, true);

        assertThat(deleted).isEqualTo(3);
        verify(todoRepository).deleteByIdIn(ids);
        verify(auditService, times(3)).log(eq("DELETE"), any(Long.class), eq("admin"));
    }
}
