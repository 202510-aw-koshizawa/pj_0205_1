package com.example.todo.controller;

import com.example.todo.dto.TodoForm;
import com.example.todo.entity.Todo;
import com.example.todo.entity.User;
import com.example.todo.service.CategoryService;
import com.example.todo.service.TodoService;
import com.example.todo.service.TodoAttachmentService;
import com.example.todo.service.FileStorageService;
import com.example.todo.entity.TodoAttachment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.nio.file.Path;

/**
 * ToDoアプリのControllerクラス
 * 画面遷移を管理するエンドポイントを定義
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/todos")
public class TodoController {

    private final TodoService todoService;
    private final CategoryService categoryService;
    private final TodoAttachmentService attachmentService;
    private final FileStorageService fileStorageService;

    /**
     * 一覧画面を表示
     * GET /todos でアクセス
     */
    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       @RequestParam(required = false) Long categoryId,
                       @RequestParam(required = false, defaultValue = "createdAt") String sort,
                       @RequestParam(required = false, defaultValue = "desc") String order,
                       @RequestParam(required = false, defaultValue = "0") int page,
                       @RequestParam(required = false, defaultValue = "10") int size,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        String sortKey = normalizeSort(sort);
        String sortOrder = normalizeOrder(order);

        org.springframework.data.domain.Sort.Direction direction =
                sortOrder.equals("asc") ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC;

        org.springframework.data.domain.Sort sortSpec = buildSort(sortKey, direction);
        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(page, size, sortSpec);

        User user = todoService.loadUser(userDetails.getUsername());
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
        org.springframework.data.domain.Page<com.example.todo.entity.Todo> todoPage =
                todoService.findPage(user, keyword, categoryId, pageable, isAdmin);

        model.addAttribute("todoPage", todoPage);
        model.addAttribute("todos", todoPage.getContent());
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("sort", sortKey);
        model.addAttribute("order", sortOrder);
        model.addAttribute("size", size);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("isAdmin", isAdmin);
        return "todo/list";
    }


    private String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace("\"", "\"\"");
    }

    private String normalizeSort(String sort) {
        if (sort == null) return "createdAt";
        switch (sort) {
            case "id":
            case "title":
            case "createdAt":
            case "completed":
            case "priority":
            case "dueDate":
                return sort;
            default:
                return "createdAt";
        }
    }

    private String normalizeOrder(String order) {
        if ("asc".equalsIgnoreCase(order)) return "asc";
        if ("desc".equalsIgnoreCase(order)) return "desc";
        return "desc";
    }

    private org.springframework.data.domain.Sort buildSort(String sortKey,
                                                          org.springframework.data.domain.Sort.Direction direction) {
        String sortColumn = "priority".equals(sortKey) ? "priorityRank" : sortKey;
        return org.springframework.data.domain.Sort.by(direction, sortColumn);
    }

    /**
     * 新規作成画面を表示
     * GET /todos/new でアクセス
     */
    @GetMapping("/new")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("todoForm", new TodoForm("", "", com.example.todo.enums.Priority.MEDIUM, null, null));
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("username", userDetails.getUsername());
        return "todo/form";
    }

    /**
     * フォームデータを受け取り確認画面へ
     * @RequestParam でフォームの各フィールドを個別に受け取る
     */
    @PostMapping("/confirm")
    public String confirm(@Valid TodoForm todoForm, BindingResult result,
                          @AuthenticationPrincipal UserDetails userDetails,
                          Model model) {
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("username", userDetails.getUsername());
            return "todo/form";
        }
        model.addAttribute("category", categoryService.findById(todoForm.getCategoryId()));
        model.addAttribute("todoForm", todoForm);
        model.addAttribute("username", userDetails.getUsername());
        return "todo/confirm";
    }

    /**
     * 詳細画面を表示
     * GET /todos/{id} でアクセス
     * @param id URLパスから取得するToDoのID
     */
    @GetMapping("/{id}")
    public String show(@PathVariable Long id,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        User user = todoService.loadUser(userDetails.getUsername());
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
        Todo todo = todoService.findByIdWithAccess(id, user, isAdmin);
        model.addAttribute("attachments", attachmentService.findByTodo(todo));
        model.addAttribute("todo", todo);
        return "todo/detail";
    }

    /**
     * 添付フォーム表示
     */
    @GetMapping("/{id}/attach")
    public String showAttachForm(@PathVariable Long id,
                                 @AuthenticationPrincipal UserDetails userDetails,
                                 Model model) {
        User user = todoService.loadUser(userDetails.getUsername());
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
        Todo todo = todoService.findByIdWithAccess(id, user, isAdmin);
        model.addAttribute("todo", todo);
        model.addAttribute("attachments", attachmentService.findByTodo(todo));
        return "todo/attach";
    }

    /**
     * 添付アップロード
     */
    @PostMapping("/{id}/attach")
    public String uploadFile(@PathVariable Long id,
                             @RequestParam("file") MultipartFile file,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (file.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "ファイルを選択してください");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/todos/" + id + "/attach";
        }
        try {
            User user = todoService.loadUser(userDetails.getUsername());
            boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
            Todo todo = todoService.findByIdWithAccess(id, user, isAdmin);
            attachmentService.store(todo, file);
            redirectAttributes.addFlashAttribute("message", "ファイルをアップロードしました");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "アップロードに失敗しました");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/todos/" + id;
    }

    /**
     * 添付ダウンロード
     */
    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long attachmentId,
                                                 @AuthenticationPrincipal UserDetails userDetails) {
        TodoAttachment attachment = attachmentService.findById(attachmentId);
        User user = todoService.loadUser(userDetails.getUsername());
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
        todoService.findByIdWithAccess(attachment.getTodo().getId(), user, isAdmin);
        try {
            Path filePath = fileStorageService.load(attachment.getStoredFilename());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists()) {
                throw new RuntimeException("ファイルが見つかりません");
            }
            String encodedFilename = URLEncoder.encode(
                    attachment.getOriginalFilename(), StandardCharsets.UTF_8
            ).replace("+", "%20");
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(attachment.getContentType()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename*=UTF-8''" + encodedFilename)
                    .body(resource);
        } catch (Exception e) {
            throw new RuntimeException("ファイルの読み込みに失敗", e);
        }
    }

    /**
     * 添付削除
     */
    @PostMapping("/attachments/{attachmentId}/delete")
    public String deleteFile(@PathVariable Long attachmentId,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        try {
            TodoAttachment attachment = attachmentService.findById(attachmentId);
            User user = todoService.loadUser(userDetails.getUsername());
            boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
            todoService.findByIdWithAccess(attachment.getTodo().getId(), user, isAdmin);
            Long todoId = attachment.getTodo().getId();
            attachmentService.delete(attachment);
            redirectAttributes.addFlashAttribute("message", "ファイルを削除しました");
            redirectAttributes.addFlashAttribute("messageType", "success");
            return "redirect:/todos/" + todoId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("message", "ファイル削除に失敗しました");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/todos";
        }
    }

    /**
     * 編集画面を表示
     */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @AuthenticationPrincipal UserDetails userDetails,
                       Model model) {
        User user = todoService.loadUser(userDetails.getUsername());
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
        Todo todo = todoService.findByIdWithAccess(id, user, isAdmin);
        TodoForm form = new TodoForm(
                todo.getTitle(),
                todo.getDescription(),
                todo.getPriority(),
                todo.getCategory() != null ? todo.getCategory().getId() : null,
                todo.getDueDate()
        );
        model.addAttribute("todoForm", form);
        model.addAttribute("todoId", todo.getId());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("username", userDetails.getUsername());
        return "todo/edit";
    }

    /**
     * 更新処理
     */
    @PostMapping("/{id}/update")
    public String update(
            @PathVariable Long id,
            @Valid TodoForm todoForm,
            BindingResult result,
            Model model,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("todoId", id);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("username", userDetails.getUsername());
            return "todo/edit";
        }

        User user = todoService.loadUser(userDetails.getUsername());
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
        todoService.update(id, todoForm.getTitle(), todoForm.getDescription(), todoForm.getPriority(),
                todoForm.getCategoryId(), todoForm.getDueDate(), user, isAdmin);
        redirectAttributes.addFlashAttribute("message", "更新が完了しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    /**
     * 完了状態の切り替え
     */
    @PostMapping("/{id}/toggle")
    public String toggleCompleted(@PathVariable Long id,
                                  @AuthenticationPrincipal UserDetails userDetails) {
        User user = todoService.loadUser(userDetails.getUsername());
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
        todoService.toggleCompleted(id, user, isAdmin);
        return "redirect:/todos";
    }

    /**
     * 登録処理を実行し完了画面へ
     */
    @PostMapping("/complete")
    public String complete(TodoForm todoForm,
                           @AuthenticationPrincipal UserDetails userDetails,
                           RedirectAttributes redirectAttributes) {
        User user = todoService.loadUser(userDetails.getUsername());
        todoService.create(todoForm, user);

        redirectAttributes.addFlashAttribute("message", "登録が完了しました");

        return "redirect:/todos";
    }

    /**
     * サンプルデータを生成
     */
    @PostMapping("/sample")
    public String createSamples(@AuthenticationPrincipal UserDetails userDetails,
                                RedirectAttributes redirectAttributes) {
        User user = todoService.loadUser(userDetails.getUsername());
        int created = todoService.createSamples(25, user);
        redirectAttributes.addFlashAttribute("message", "サンプルを" + created + "件作成しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    /**
     * 削除処理
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @AuthenticationPrincipal UserDetails userDetails,
                         RedirectAttributes redirectAttributes) {
        try {
            User user = todoService.loadUser(userDetails.getUsername());
            boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
            todoService.delete(id, user, isAdmin);
            redirectAttributes.addFlashAttribute("message", "ToDoを削除しました");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "削除に失敗しました");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/todos";
    }

    /**
     * 一括削除
     */
    @PostMapping("/bulk-delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String bulkDelete(@RequestParam(required = false) List<Long> ids,
                             @AuthenticationPrincipal UserDetails userDetails,
                             RedirectAttributes redirectAttributes) {
        if (ids == null || ids.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "削除する項目を選択してください");
            redirectAttributes.addFlashAttribute("messageType", "danger");
            return "redirect:/todos";
        }
        User user = todoService.loadUser(userDetails.getUsername());
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
        int count = todoService.deleteByIds(ids, user, isAdmin);
        redirectAttributes.addFlashAttribute("message", count + "件を削除しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    /**
     * CSVエクスポート
     */
    @GetMapping("/export")
    public void exportCsv(@RequestParam(required = false) String keyword,
                          @RequestParam(required = false) Long categoryId,
                          @RequestParam(required = false, defaultValue = "createdAt") String sort,
                          @RequestParam(required = false, defaultValue = "desc") String order,
                          @AuthenticationPrincipal UserDetails userDetails,
                          HttpServletResponse response) throws Exception {
        String sortKey = normalizeSort(sort);
        String sortOrder = normalizeOrder(order);
        org.springframework.data.domain.Sort.Direction direction =
                sortOrder.equals("asc") ? org.springframework.data.domain.Sort.Direction.ASC
                        : org.springframework.data.domain.Sort.Direction.DESC;
        org.springframework.data.domain.Sort sortSpec = buildSort(sortKey, direction);

        User user = todoService.loadUser(userDetails.getUsername());
        boolean isAdmin = hasRole(userDetails, "ROLE_ADMIN");
        List<Todo> todos = todoService.findAll(user, keyword, categoryId, sortSpec, isAdmin);

        String filename = URLEncoder.encode("todo_" +
                java.time.LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE) + ".csv",
                StandardCharsets.UTF_8);
        response.setContentType("text/csv; charset=UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        PrintWriter writer = response.getWriter();
        writer.print('\ufeff');
        writer.println("ID,タイトル,説明,優先度,状態,カテゴリ,期限日,作成日");

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        for (Todo todo : todos) {
            String due = todo.getDueDate() != null ? todo.getDueDate().format(dateFormatter) : "";
            String created = todo.getCreatedAt() != null ? todo.getCreatedAt().format(dateFormatter) : "";
            String priority = todo.getPriority() != null ? todo.getPriority().getDisplayName() : "";
            String status = Boolean.TRUE.equals(todo.getCompleted()) ? "完了" : "未完了";
            String category = todo.getCategory() != null ? todo.getCategory().getName() : "";

            writer.println(String.format("%d,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"",
                    todo.getId(),
                    escapeCsv(todo.getTitle()),
                    escapeCsv(todo.getDescription()),
                    escapeCsv(priority),
                    escapeCsv(status),
                    escapeCsv(category),
                    escapeCsv(due),
                    escapeCsv(created)
            ));
        }
        writer.flush();
    }

    private boolean hasRole(UserDetails userDetails, String role) {
        for (GrantedAuthority authority : userDetails.getAuthorities()) {
            if (authority.getAuthority().equals(role)) {
                return true;
            }
        }
        return false;
    }
}
