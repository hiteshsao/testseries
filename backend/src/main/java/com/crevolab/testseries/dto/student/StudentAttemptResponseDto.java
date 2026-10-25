package com.crevolab.testseries.dto.student;

import com.crevolab.testseries.domain.enums.AttemptStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class StudentAttemptResponseDto {

    private Long attemptId;
    private Long testId;
    private String testTitle;
    private String studentId;
    private Instant startedAt;
    private Instant endsAt;
    private Long remainingSeconds;
    private Integer durationMinutes;
    private AttemptStatus status;
    private BigDecimal totalMarks;
    private List<StudentQuestionDto> questions = new ArrayList<>();
    private List<StudentAnswerDto> answers = new ArrayList<>();

    public StudentAttemptResponseDto() {
    }

    public StudentAttemptResponseDto(Long attemptId, Long testId, String testTitle,
                                     String studentId, Instant startedAt, Instant endsAt,
                                     Long remainingSeconds, Integer durationMinutes,
                                     AttemptStatus status, BigDecimal totalMarks,
                                     List<StudentQuestionDto> questions,
                                     List<StudentAnswerDto> answers) {
        this.attemptId = attemptId;
        this.testId = testId;
        this.testTitle = testTitle;
        this.studentId = studentId;
        this.startedAt = startedAt;
        this.endsAt = endsAt;
        this.remainingSeconds = remainingSeconds;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.totalMarks = totalMarks;
        this.questions = questions != null ? questions : new ArrayList<>();
        this.answers = answers != null ? answers : new ArrayList<>();
    }

    public Long getAttemptId() {
        return attemptId;
    }

    public void setAttemptId(Long attemptId) {
        this.attemptId = attemptId;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getTestTitle() {
        return testTitle;
    }

    public void setTestTitle(String testTitle) {
        this.testTitle = testTitle;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Instant startedAt) {
        this.startedAt = startedAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(Instant endsAt) {
        this.endsAt = endsAt;
    }

    public Long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public AttemptStatus getStatus() {
        return status;
    }

    public void setStatus(AttemptStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(BigDecimal totalMarks) {
        this.totalMarks = totalMarks;
    }

    public List<StudentQuestionDto> getQuestions() {
        return questions;
    }

    public void setQuestions(List<StudentQuestionDto> questions) {
        this.questions = questions;
    }

    public List<StudentAnswerDto> getAnswers() {
        return answers;
    }

    public void setAnswers(List<StudentAnswerDto> answers) {
        this.answers = answers;
    }
}
