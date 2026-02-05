package com.example.todo.controller;

import com.example.todo.dto.TodoForm;
import com.example.todo.entity.Todo;
import com.example.todo.service.TodoService;
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

/**
 * ToDoアプリのControllerクラス
 * 画面遷移を管理するエンドポイントを定義
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/todos")
public class TodoController {

    private final TodoService todoService;

    /**
     * 一覧画面を表示
     * GET /todos でアクセス
     */
    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                       Model model) {
        if (keyword != null && !keyword.isBlank()) {
            model.addAttribute("todos", todoService.searchByTitle(keyword.trim()));
        } else {
            model.addAttribute("todos", todoService.findAll());
        }
        model.addAttribute("keyword", keyword);
        return "todo/list";
    }

    /**
     * 新規作成画面を表示
     * GET /todos/new でアクセス
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("todoForm", new TodoForm("", "", 3));
        return "todo/form";
    }

    /**
     * フォームデータを受け取り確認画面へ
     * @RequestParam でフォームの各フィールドを個別に受け取る
     */
    @PostMapping("/confirm")
    public String confirm(@Valid TodoForm todoForm, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "todo/form";
        }
        model.addAttribute("todoForm", todoForm);
        return "todo/confirm";
    }

    /**
     * 詳細画面を表示
     * GET /todos/{id} でアクセス
     * @param id URLパスから取得するToDoのID
     */
    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        // IDに対応するToDoを取得してModelに追加
        return "todo/detail";
    }

    /**
     * 編集画面を表示
     */
    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        Todo todo = todoService.findById(id);
        TodoForm form = new TodoForm(todo.getTitle(), todo.getDescription(), todo.getPriority());
        model.addAttribute("todoForm", form);
        model.addAttribute("todoId", todo.getId());
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
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("todoId", id);
            return "todo/edit";
        }

        todoService.update(id, todoForm.getTitle(), todoForm.getDescription(), todoForm.getPriority());
        redirectAttributes.addFlashAttribute("message", "更新が完了しました");
        redirectAttributes.addFlashAttribute("messageType", "success");
        return "redirect:/todos";
    }

    /**
     * 完了状態の切り替え
     */
    @PostMapping("/{id}/toggle")
    public String toggleCompleted(@PathVariable Long id) {
        todoService.toggleCompleted(id);
        return "redirect:/todos";
    }

    /**
     * 登録処理を実行し完了画面へ
     */
    @PostMapping("/complete")
    public String complete(TodoForm todoForm, RedirectAttributes redirectAttributes) {
        todoService.create(todoForm);

        redirectAttributes.addFlashAttribute("message", "登録が完了しました");

        return "redirect:/todos";
    }

    /**
     * 削除処理
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            todoService.delete(id);
            redirectAttributes.addFlashAttribute("message", "ToDoを削除しました");
            redirectAttributes.addFlashAttribute("messageType", "success");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("message", "削除に失敗しました");
            redirectAttributes.addFlashAttribute("messageType", "danger");
        }
        return "redirect:/todos";
    }
}
