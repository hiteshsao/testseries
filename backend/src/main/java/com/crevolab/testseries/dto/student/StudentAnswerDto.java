package com.crevolab.testseries.dto.student;

public class StudentAnswerDto {

    private Long questionId;
    private String selectedOption;
    private Boolean markedForReview;

    public StudentAnswerDto() {
    }

    public StudentAnswerDto(Long questionId, String selectedOption, Boolean markedForReview) {
        this.questionId = questionId;
        this.selectedOption = selectedOption;
        this.markedForReview = markedForReview != null ? markedForReview : false;
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
}
