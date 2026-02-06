package com.example.todo.scheduler;

import com.example.todo.entity.Todo;
import com.example.todo.repository.TodoRepository;
import com.example.todo.service.MailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class ReminderScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReminderScheduler.class);

    private final TodoRepository todoRepository;
    private final MailService mailService;

    public ReminderScheduler(TodoRepository todoRepository, MailService mailService) {
        this.todoRepository = todoRepository;
        this.mailService = mailService;
    }

    // 毎日9時に「明日が期限」のToDoを通知（ログのみ）
    @Scheduled(cron = "0 0 9 * * *")
    public void sendDailyReminders() {
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        List<Todo> todos = todoRepository.findByDueDateAndCompletedFalse(tomorrow);
        if (todos.isEmpty()) {
            return;
        }
        logger.info("ReminderScheduler: {} todos due tomorrow", todos.size());
        for (Todo todo : todos) {
            if (todo.getUser() != null) {
                mailService.sendDeadlineReminderAsync(todo.getUser(), todo);
            }
        }
    }
}
