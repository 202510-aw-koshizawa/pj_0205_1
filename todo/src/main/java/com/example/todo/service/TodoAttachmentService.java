package com.example.todo.service;

import com.example.todo.entity.Todo;
import com.example.todo.entity.TodoAttachment;
import com.example.todo.repository.TodoAttachmentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoAttachmentService {

    private final TodoAttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;

    @Transactional
    public TodoAttachment store(Todo todo, MultipartFile file) throws IOException {
        String storedFilename = fileStorageService.store(file);

        TodoAttachment attachment = new TodoAttachment();
        attachment.setTodo(todo);
        attachment.setOriginalFilename(file.getOriginalFilename());
        attachment.setStoredFilename(storedFilename);
        attachment.setContentType(file.getContentType());
        attachment.setFileSize(file.getSize());
        return attachmentRepository.save(attachment);
    }

    @Transactional(readOnly = true)
    public List<TodoAttachment> findByTodo(Todo todo) {
        return attachmentRepository.findByTodoOrderByUploadedAtDesc(todo);
    }

    @Transactional(readOnly = true)
    public TodoAttachment findById(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new com.example.todo.exception.BusinessException("E404", "添付ファイルが見つかりません"));
    }

    @Transactional
    public void delete(TodoAttachment attachment) throws IOException {
        fileStorageService.delete(attachment.getStoredFilename());
        attachmentRepository.delete(attachment);
    }
}
