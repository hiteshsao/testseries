package com.crevolab.testseries.dto.admin;

import com.crevolab.testseries.domain.TestSeries;
import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.domain.enums.TestStatus;

import java.math.BigDecimal;
import java.time.Instant;

public class TestResponseDto {

    private Long id;
    private String title;
    private String instructions;
    private Integer durationMinutes;
    private TestStatus status;
    private AccessType accessType;
    private BigDecimal price;
    private Integer maxAttempts;
    private Integer maxSolutionViews;
    private Integer maxPdfDownloads;
    private String sourcePdfPath;
    private String solutionPdfPath;
    private Instant createdAt;

    public TestResponseDto() {
    }

    public static TestResponseDto fromEntity(TestSeries entity) {
        if (entity == null) {
            return null;
        }
        TestResponseDto dto = new TestResponseDto();
        dto.setId(entity.getId());
        dto.setTitle(entity.getTitle());
        dto.setInstructions(entity.getInstructions());
        dto.setDurationMinutes(entity.getDurationMinutes());
        dto.setStatus(entity.getStatus());
        dto.setAccessType(entity.getAccessType());
        dto.setPrice(entity.getPrice());
        dto.setMaxAttempts(entity.getMaxAttempts());
        dto.setMaxSolutionViews(entity.getMaxSolutionViews());
        dto.setMaxPdfDownloads(entity.getMaxPdfDownloads());
        dto.setSourcePdfPath(entity.getSourcePdfPath());
        dto.setSolutionPdfPath(entity.getSolutionPdfPath());
        dto.setCreatedAt(entity.getCreatedAt());
        return dto;
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

    public Integer getMaxSolutionViews() {
        return maxSolutionViews;
    }

    public void setMaxSolutionViews(Integer maxSolutionViews) {
        this.maxSolutionViews = maxSolutionViews;
    }

    public Integer getMaxPdfDownloads() {
        return maxPdfDownloads;
    }

    public void setMaxPdfDownloads(Integer maxPdfDownloads) {
        this.maxPdfDownloads = maxPdfDownloads;
    }

    public String getSourcePdfPath() {
        return sourcePdfPath;
    }

    public void setSourcePdfPath(String sourcePdfPath) {
        this.sourcePdfPath = sourcePdfPath;
    }

    public String getSolutionPdfPath() {
        return solutionPdfPath;
    }

    public void setSolutionPdfPath(String solutionPdfPath) {
        this.solutionPdfPath = solutionPdfPath;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
