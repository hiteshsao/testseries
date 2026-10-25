package com.crevolab.testseries.controller.admin;

import com.crevolab.testseries.dto.admin.QuestionRequestDto;
import com.crevolab.testseries.dto.admin.QuestionResponseDto;
import com.crevolab.testseries.service.AdminQuestionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class AdminQuestionController {

    private final AdminQuestionService adminQuestionService;

    public AdminQuestionController(AdminQuestionService adminQuestionService) {
        this.adminQuestionService = adminQuestionService;
    }

    @GetMapping("/api/admin/tests/{id}/questions")
    public ResponseEntity<List<QuestionResponseDto>> getQuestionsByTestId(@PathVariable Long id) {
        return ResponseEntity.ok(adminQuestionService.getQuestionsByTestId(id));
    }

    @PostMapping("/api/admin/tests/{id}/questions")
    public ResponseEntity<QuestionResponseDto> createQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequestDto request) {
        QuestionResponseDto created = adminQuestionService.createQuestion(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/api/admin/questions/{id}")
    public ResponseEntity<QuestionResponseDto> getQuestionById(@PathVariable Long id) {
        return ResponseEntity.ok(adminQuestionService.getQuestionById(id));
    }

    @PutMapping("/api/admin/questions/{id}")
    public ResponseEntity<QuestionResponseDto> updateQuestion(
            @PathVariable Long id,
            @Valid @RequestBody QuestionRequestDto request) {
        return ResponseEntity.ok(adminQuestionService.updateQuestion(id, request));
    }

    @DeleteMapping("/api/admin/questions/{id}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Long id) {
        adminQuestionService.deleteQuestion(id);
        return ResponseEntity.noContent().build();
    }
}
