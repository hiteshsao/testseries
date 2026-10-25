package com.crevolab.testseries.controller.student;

import com.crevolab.testseries.dto.student.StudentAnswerRequestDto;
import com.crevolab.testseries.dto.student.StudentAnswerResponseDto;
import com.crevolab.testseries.dto.student.StudentAttemptResponseDto;
import com.crevolab.testseries.dto.student.StudentTestSummaryDto;
import com.crevolab.testseries.service.StudentTestService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    // DEMO-ONLY: Simulated student header identifier
    public static final String DEMO_STUDENT_HEADER = "X-Demo-Student-Id";
    public static final String DEFAULT_DEMO_STUDENT_ID = "demo-student-1";

    private final StudentTestService studentTestService;

    public StudentController(StudentTestService studentTestService) {
        this.studentTestService = studentTestService;
    }

    // DEMO-ONLY: Extract student id from X-Demo-Student-Id header
    private String resolveStudentId(String studentHeader) {
        if (studentHeader != null && !studentHeader.trim().isEmpty()) {
            return studentHeader.trim();
        }
        return DEFAULT_DEMO_STUDENT_ID;
    }

    /**
     * GET /api/student/tests (PUBLISHED only)
     */
    @GetMapping("/tests")
    public ResponseEntity<List<StudentTestSummaryDto>> getPublishedTests(
            // DEMO-ONLY: Student ID extracted from X-Demo-Student-Id demo header
            @RequestHeader(value = DEMO_STUDENT_HEADER, required = false) String studentHeader) {
        String studentId = resolveStudentId(studentHeader);
        return ResponseEntity.ok(studentTestService.getPublishedTests(studentId));
    }

    /**
     * POST /api/student/tests/{id}/start
     * Creates attempt, sets ends_at = now + duration (server clock),
     * enforces max_attempts and paid-purchase check; returns questions WITHOUT correct_option, marks only.
     */
    @PostMapping("/tests/{id}/start")
    public ResponseEntity<StudentAttemptResponseDto> startTest(
            @PathVariable Long id,
            // DEMO-ONLY: Student ID extracted from X-Demo-Student-Id demo header
            @RequestHeader(value = DEMO_STUDENT_HEADER, required = false) String studentHeader) {
        String studentId = resolveStudentId(studentHeader);
        return ResponseEntity.ok(studentTestService.startTest(id, studentId));
    }

    /**
     * PUT /api/student/attempts/{id}/answer
     * Saves selected option and marked_for_review; rejects after ends_at or if submitted.
     */
    @PutMapping("/attempts/{id}/answer")
    public ResponseEntity<StudentAnswerResponseDto> saveAnswer(
            @PathVariable Long id,
            @Valid @RequestBody StudentAnswerRequestDto request,
            // DEMO-ONLY: Student ID extracted from X-Demo-Student-Id demo header
            @RequestHeader(value = DEMO_STUDENT_HEADER, required = false) String studentHeader) {
        String studentId = resolveStudentId(studentHeader);
        return ResponseEntity.ok(studentTestService.saveAnswer(id, studentId, request));
    }

    /**
     * GET /api/student/attempts/{id}
     * Resumes the attempt (answers + remaining seconds).
     */
    @GetMapping("/attempts/{id}")
    public ResponseEntity<StudentAttemptResponseDto> getAttempt(
            @PathVariable Long id,
            // DEMO-ONLY: Student ID extracted from X-Demo-Student-Id demo header
            @RequestHeader(value = DEMO_STUDENT_HEADER, required = false) String studentHeader) {
        String studentId = resolveStudentId(studentHeader);
        return ResponseEntity.ok(studentTestService.getAttempt(id, studentId));
    }
}
