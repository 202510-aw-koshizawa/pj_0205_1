package com.example.todo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ToDoアプリのControllerクラス
 * 画面遷移を管理するエンドポイントを定義
 */
@Controller
@RequestMapping("/todos")
public class TodoController {

    /**
     * 一覧画面を表示
     * GET /todos でアクセス
     */
    @GetMapping
    public String list(Model model) {
        // ToDoリストをModelに追加（後でServiceから取得）
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
     * 詳細画面を表示
     * GET /todos/{id} でアクセス
     * @param id URLパスから取得するToDoのID
     */
    @GetMapping("/{id}")
    public String show(@PathVariable Long id, Model model) {
        // IDに対応するToDoを取得してModelに追加
        return "todo/detail";
    }
}
