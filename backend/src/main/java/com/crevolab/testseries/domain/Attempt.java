package com.crevolab.testseries.domain;

import com.crevolab.testseries.domain.enums.AttemptStatus;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "attempts")
public class Attempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private TestSeries test;

    @Column(name = "student_id", nullable = false, length = 100)
    private String studentId;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt = Instant.now();

    @Column(name = "ends_at", nullable = false)
    private Instant endsAt;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AttemptStatus status = AttemptStatus.IN_PROGRESS;

    @Column(name = "total_marks", nullable = false, precision = 7, scale = 2)
    private BigDecimal totalMarks = BigDecimal.ZERO;

    @Column(name = "marks_obtained", nullable = false, precision = 7, scale = 2)
    private BigDecimal marksObtained = BigDecimal.ZERO;

    @Column(nullable = false)
    private Integer correct = 0;

    @Column(nullable = false)
    private Integer incorrect = 0;

    @Column(nullable = false)
    private Integer unanswered = 0;

    public Attempt() {
    }

    public Attempt(TestSeries test, String studentId, Instant startedAt, Instant endsAt,
                   AttemptStatus status, BigDecimal totalMarks) {
        this.test = test;
        this.studentId = studentId;
        this.startedAt = startedAt != null ? startedAt : Instant.now();
        this.endsAt = endsAt;
        this.status = status != null ? status : AttemptStatus.IN_PROGRESS;
        this.totalMarks = totalMarks != null ? totalMarks : BigDecimal.ZERO;
        this.marksObtained = BigDecimal.ZERO;
        this.correct = 0;
        this.incorrect = 0;
        this.unanswered = 0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestSeries getTest() {
        return test;
    }

    public void setTest(TestSeries test) {
        this.test = test;
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
}
