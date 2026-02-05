package com.example.todo.controller;

import com.example.todo.dto.TodoForm;
import com.example.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
    public String list(Model model) {
        model.addAttribute("todos", todoService.findAll());
        return "todo/list";
    }

    /**
     * 新規作成画面を表示
     * GET /todos/new でアクセス
     */
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        // 空のToDoオブジェクトをフォーム用にセット
        return "todo/form";
    }

    /**
     * フォームデータを受け取り確認画面へ
     * @RequestParam でフォームの各フィールドを個別に受け取る
     */
    @PostMapping("/confirm")
    public String confirm(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "priority", defaultValue = "3") Integer priority,
            Model model) {

        // 受け取ったデータをModelに格納
        model.addAttribute("title", title);
        model.addAttribute("description", description);
        model.addAttribute("priority", priority);

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
     * 登録処理を実行し完了画面へ
     */
    @PostMapping("/complete")
    public String complete(
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "priority", defaultValue = "3") Integer priority,
            RedirectAttributes redirectAttributes) {

        TodoForm form = new TodoForm(title, description, priority);
        todoService.create(form);

        redirectAttributes.addFlashAttribute("message", "登録が完了しました");

        return "redirect:/todos";
    }
}
