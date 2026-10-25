package com.crevolab.testseries.dto.admin;

public class AdminStatsDto {

    private long totalTests;
    private long publishedTests;
    private long draftTests;
    private long totalAttempts;

    public AdminStatsDto() {
    }

    public AdminStatsDto(long totalTests, long publishedTests, long draftTests, long totalAttempts) {
        this.totalTests = totalTests;
        this.publishedTests = publishedTests;
        this.draftTests = draftTests;
        this.totalAttempts = totalAttempts;
    }

    public long getTotalTests() {
        return totalTests;
    }

    public void setTotalTests(long totalTests) {
        this.totalTests = totalTests;
    }

    public long getPublishedTests() {
        return publishedTests;
    }

    public void setPublishedTests(long publishedTests) {
        this.publishedTests = publishedTests;
    }

    public long getDraftTests() {
        return draftTests;
    }

    public void setDraftTests(long draftTests) {
        this.draftTests = draftTests;
    }

    public long getTotalAttempts() {
        return totalAttempts;
    }

    public void setTotalAttempts(long totalAttempts) {
        this.totalAttempts = totalAttempts;
    }
}
