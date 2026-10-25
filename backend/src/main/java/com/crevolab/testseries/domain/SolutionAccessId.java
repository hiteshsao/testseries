package com.crevolab.testseries.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class SolutionAccessId implements Serializable {

    @Column(name = "test_id")
    private Long testId;

    @Column(name = "student_id", length = 100)
    private String studentId;

    public SolutionAccessId() {
    }

    public SolutionAccessId(Long testId, String studentId) {
        this.testId = testId;
        this.studentId = studentId;
    }

    public Long getTestId() {
        return testId;
    }

    public void setTestId(Long testId) {
        this.testId = testId;
    }

    public String getStudentId() {
        return studentId;
    }

    public void setStudentId(String studentId) {
        this.studentId = studentId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SolutionAccessId that = (SolutionAccessId) o;
        return Objects.equals(testId, that.testId) && Objects.equals(studentId, that.studentId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testId, studentId);
    }
}
