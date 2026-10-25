package com.crevolab.testseries.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "attempt_answers")
public class AttemptAnswer {

    @EmbeddedId
    private AttemptAnswerId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("attemptId")
    @JoinColumn(name = "attempt_id", nullable = false)
    private Attempt attempt;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("questionId")
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "selected_option", length = 1)
    private String selectedOption; // Nullable: 'A', 'B', 'C', 'D'

    @Column(name = "marked_for_review", nullable = false)
    private Boolean markedForReview = false;

    public AttemptAnswer() {
    }

    public AttemptAnswer(Attempt attempt, Question question, String selectedOption, Boolean markedForReview) {
        this.attempt = attempt;
        this.question = question;
        this.id = new AttemptAnswerId(attempt.getId(), question.getId());
        this.selectedOption = selectedOption;
        this.markedForReview = markedForReview != null ? markedForReview : false;
    }

    public AttemptAnswerId getId() {
        return id;
    }

    public void setId(AttemptAnswerId id) {
        this.id = id;
    }

    public Attempt getAttempt() {
        return attempt;
    }

    public void setAttempt(Attempt attempt) {
        this.attempt = attempt;
        if (attempt != null && this.id != null) {
            this.id.setAttemptId(attempt.getId());
        }
    }

    public Question getQuestion() {
        return question;
    }

    public void setQuestion(Question question) {
        this.question = question;
        if (question != null && this.id != null) {
            this.id.setQuestionId(question.getId());
        }
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
