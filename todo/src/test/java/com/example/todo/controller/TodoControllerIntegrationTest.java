package com.example.todo.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class TodoControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("未ログインの場合は /todos がログインへリダイレクトされる")
    void list_requires_login() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @DisplayName("ログイン済みなら一覧ページが表示される")
    @WithMockUser(username = "user", roles = "USER")
    void list_page_ok() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isOk())
                .andExpect(view().name("todo/list"))
                .andExpect(model().attributeExists("todos"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @DisplayName("ログイン画面が表示される")
    void login_page_ok() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }
}
