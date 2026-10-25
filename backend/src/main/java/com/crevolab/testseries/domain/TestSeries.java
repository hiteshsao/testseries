package com.crevolab.testseries.domain;

import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.domain.enums.TestStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "tests")
public class TestSeries {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TestStatus status = TestStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "access_type", nullable = false, length = 20)
    private AccessType accessType = AccessType.FREE;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price = BigDecimal.ZERO;

    @Column(name = "max_attempts", nullable = false)
    private Integer maxAttempts = 1;

    @Column(name = "max_solution_views", nullable = false)
    private Integer maxSolutionViews = 3;

    @Column(name = "max_pdf_downloads", nullable = false)
    private Integer maxPdfDownloads = 1;

    @Column(name = "source_pdf_path", length = 500)
    private String sourcePdfPath;

    @Column(name = "solution_pdf_path", length = 500)
    private String solutionPdfPath;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    public TestSeries() {
    }

    public TestSeries(String title, String instructions, Integer durationMinutes, TestStatus status,
                      AccessType accessType, BigDecimal price, Integer maxAttempts, Integer maxSolutionViews,
                      Integer maxPdfDownloads, String sourcePdfPath, String solutionPdfPath) {
        this.title = title;
        this.instructions = instructions;
        this.durationMinutes = durationMinutes;
        this.status = status;
        this.accessType = accessType;
        this.price = price != null ? price : BigDecimal.ZERO;
        this.maxAttempts = maxAttempts != null ? maxAttempts : 1;
        this.maxSolutionViews = maxSolutionViews != null ? maxSolutionViews : 3;
        this.maxPdfDownloads = maxPdfDownloads != null ? maxPdfDownloads : 1;
        this.sourcePdfPath = sourcePdfPath;
        this.solutionPdfPath = solutionPdfPath;
        this.createdAt = Instant.now();
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
