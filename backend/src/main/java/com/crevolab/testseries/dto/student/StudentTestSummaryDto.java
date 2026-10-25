package com.crevolab.testseries.dto.student;

import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.domain.enums.TestStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class StudentTestSummaryDto {

    private Long id;
    private String title;
    private String instructions;
    private Integer durationMinutes;
    private TestStatus status;
    private AccessType accessType;
    private BigDecimal price;
    private Integer maxAttempts;
    private Long attemptsUsed;
    private Boolean purchased;
    private Boolean canAttempt;
    private Integer totalQuestions;
    private BigDecimal totalMarks;
    private Instant createdAt;

    public StudentTestSummaryDto() {
    }

    public StudentTestSummaryDto(Long id, String title, String instructions, Integer durationMinutes,
                                 TestStatus status, AccessType accessType, BigDecimal price,
                                 Integer maxAttempts, Long attemptsUsed, Boolean purchased,
                                 Boolean canAttempt, Integer totalQuestions, BigDecimal totalMarks,
                                 Instant createdAt) {
        this.id = id;
        this.title = title;
        this.instructions = instructions;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.accessType = accessType;
        this.price = price;
        this.maxAttempts = maxAttempts;
        this.attemptsUsed = attemptsUsed;
        this.purchased = purchased;
        this.canAttempt = canAttempt;
        this.totalQuestions = totalQuestions;
        this.totalMarks = totalMarks;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public Integer getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public TestStatus getStatus() {
        return status;
    }

    public void setStatus(TestStatus status) {
        this.status = status;
    }

    public AccessType getAccessType() {
        return accessType;
    }

    public void setAccessType(AccessType accessType) {
        this.accessType = accessType;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(Integer maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public Long getAttemptsUsed() {
        return attemptsUsed;
    }

    public void setAttemptsUsed(Long attemptsUsed) {
        this.attemptsUsed = attemptsUsed;
    }

    public Boolean getPurchased() {
        return purchased;
    }

    public void setPurchased(Boolean purchased) {
        this.purchased = purchased;
    }

    public Boolean getCanAttempt() {
        return canAttempt;
    }

    public void setCanAttempt(Boolean canAttempt) {
        this.canAttempt = canAttempt;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public BigDecimal getTotalMarks() {
        return totalMarks;
    }

    public void setTotalMarks(BigDecimal totalMarks) {
        this.totalMarks = totalMarks;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
