package com.example.todo.controller;

import com.example.todo.entity.AuditLog;
import com.example.todo.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/audit-logs")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    public String list(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            Model model
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        boolean hasAction = action != null && !action.isBlank();
        boolean hasUsername = username != null && !username.isBlank();

        Page<AuditLog> logs;
        if (hasAction && hasUsername) {
            logs = auditLogRepository.findByActionIgnoreCaseAndUsernameContainingIgnoreCase(
                    action.trim(),
                    username.trim(),
                    pageable
            );
        } else if (hasAction) {
            logs = auditLogRepository.findByActionIgnoreCase(action.trim(), pageable);
        } else if (hasUsername) {
            logs = auditLogRepository.findByUsernameContainingIgnoreCase(username.trim(), pageable);
        } else {
            logs = auditLogRepository.findAll(pageable);
        }

        model.addAttribute("logs", logs);
        model.addAttribute("action", action);
        model.addAttribute("username", username);

        return "admin/audit-logs";
    }
}
