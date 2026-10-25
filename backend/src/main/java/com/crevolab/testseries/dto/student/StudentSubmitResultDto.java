package com.crevolab.testseries.dto.student;

import com.crevolab.testseries.domain.enums.AttemptStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class StudentSubmitResultDto {

    private Long attemptId;
    private Long testId;
    private String testTitle;
    private String studentId;
    private Instant startedAt;
    private Instant submittedAt;
    private AttemptStatus status;
    private BigDecimal totalMarks;
    private BigDecimal marksObtained;
    private Integer totalQuestions;
    private Integer correct;
    private Integer incorrect;
    private Integer unanswered;
    private Double percentage;

    public StudentSubmitResultDto() {
    }

    public StudentSubmitResultDto(Long attemptId, Long testId, String testTitle, String studentId,
                                  Instant startedAt, Instant submittedAt, AttemptStatus status,
                                  BigDecimal totalMarks, BigDecimal marksObtained,
                                  Integer totalQuestions, Integer correct, Integer incorrect,
                                  Integer unanswered, Double percentage) {
        this.attemptId = attemptId;
        this.testId = testId;
        this.testTitle = testTitle;
        this.studentId = studentId;
        this.startedAt = startedAt;
        this.submittedAt = submittedAt;
        this.status = status;
        this.totalMarks = totalMarks;
        this.marksObtained = marksObtained;
        this.totalQuestions = totalQuestions;
        this.correct = correct;
        this.incorrect = incorrect;
        this.unanswered = unanswered;
        this.percentage = percentage;
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

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public void setSubmittedAt(Instant submittedAt) {
        this.submittedAt = submittedAt;
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

    public BigDecimal getMarksObtained() {
        return marksObtained;
    }

    public void setMarksObtained(BigDecimal marksObtained) {
        this.marksObtained = marksObtained;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getCorrect() {
        return correct;
    }

    public void setCorrect(Integer correct) {
        this.correct = correct;
    }

    public Integer getIncorrect() {
        return incorrect;
    }

    public void setIncorrect(Integer incorrect) {
        this.incorrect = incorrect;
    }

    public Integer getUnanswered() {
        return unanswered;
    }

    public void setUnanswered(Integer unanswered) {
        this.unanswered = unanswered;
    }

    public Double getPercentage() {
        return percentage;
    }

    public void setPercentage(Double percentage) {
        this.percentage = percentage;
    }
}
