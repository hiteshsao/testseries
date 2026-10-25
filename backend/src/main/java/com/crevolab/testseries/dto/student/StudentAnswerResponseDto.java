package com.crevolab.testseries.dto.student;

public class StudentAnswerResponseDto {

    private Long questionId;
    private String selectedOption;
    private Boolean markedForReview;
    private Long remainingSeconds;

    public StudentAnswerResponseDto() {
    }

    public StudentAnswerResponseDto(Long questionId, String selectedOption, Boolean markedForReview, Long remainingSeconds) {
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.markedForReview = markedForReview != null ? markedForReview : false;
        this.remainingSeconds = remainingSeconds;
    }

    public Long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Long questionId) {
        this.questionId = questionId;
    }

    public String getSelectedOption() {
        return selectedOption;
    }

    public void setSelectedOption(String selectedOption) {
        this.selectedOption = selectedOption;
    }

    public Boolean getMarkedForReview() {
        return markedForReview;
    }

    public void setMarkedForReview(Boolean markedForReview) {
        this.markedForReview = markedForReview;
    }

    public Long getRemainingSeconds() {
        return remainingSeconds;
    }

    public void setRemainingSeconds(Long remainingSeconds) {
        this.remainingSeconds = remainingSeconds;
    }
}
