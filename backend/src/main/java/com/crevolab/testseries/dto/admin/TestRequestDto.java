package com.crevolab.testseries.dto.admin;

import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.domain.enums.TestStatus;
import com.crevolab.testseries.dto.validation.ValidTestPricing;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

@ValidTestPricing
public class TestRequestDto {

    @NotBlank(message = "Title is required")
    private String title;

    private String instructions;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    private Integer durationMinutes;

    private TestStatus status = TestStatus.DRAFT;

    private AccessType accessType = AccessType.FREE;

    private BigDecimal price = BigDecimal.ZERO;

    @NotNull(message = "Max attempts is required")
    @Min(value = 1, message = "Max attempts must be at least 1")
    private Integer maxAttempts = 1;

    @NotNull(message = "Max solution views is required")
    @Min(value = 0, message = "Max solution views must be at least 0")
    private Integer maxSolutionViews = 3;

    @NotNull(message = "Max PDF downloads is required")
    @Min(value = 0, message = "Max PDF downloads must be at least 0")
    private Integer maxPdfDownloads = 1;

    private String sourcePdfPath;

    private String solutionPdfPath;

    public TestRequestDto() {
    }

    public TestRequestDto(String title, String instructions, Integer durationMinutes, TestStatus status,
                          AccessType accessType, BigDecimal price, Integer maxAttempts,
                          Integer maxSolutionViews, Integer maxPdfDownloads,
                          String sourcePdfPath, String solutionPdfPath) {
        this.title = title;
        this.instructions = instructions;
        this.durationMinutes = durationMinutes;
        this.status = status != null ? status : TestStatus.DRAFT;
        this.accessType = accessType != null ? accessType : AccessType.FREE;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.maxAttempts = maxAttempts != null ? maxAttempts : 1;
        this.maxSolutionViews = maxSolutionViews != null ? maxSolutionViews : 3;
        this.maxPdfDownloads = maxPdfDownloads != null ? maxPdfDownloads : 1;
        this.sourcePdfPath = sourcePdfPath;
        this.solutionPdfPath = solutionPdfPath;
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
}
