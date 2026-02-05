package com.example.todo.service;

import com.example.todo.entity.AuditLog;
import com.example.todo.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String action, Long todoId, String username) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setTodoId(todoId);
        log.setUsername(username);
        auditLogRepository.save(log);
    }
}
