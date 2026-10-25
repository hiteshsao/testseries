package com.crevolab.testseries.domain;

import jakarta.persistence.*;

import java.math.BigDecimal;

@Entity
@Table(name = "questions")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "test_id", nullable = false)
    private TestSeries test;

    @Column(name = "order_no", nullable = false)
    private Integer orderNo;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(name = "option_a", columnDefinition = "TEXT", nullable = false)
    private String optionA;

    @Column(name = "option_b", columnDefinition = "TEXT", nullable = false)
    private String optionB;

    @Column(name = "option_c", columnDefinition = "TEXT", nullable = false)
    private String optionC;

    @Column(name = "option_d", columnDefinition = "TEXT", nullable = false)
    private String optionD;

    @Column(name = "correct_option", length = 1)
    private String correctOption; // Nullable: 'A', 'B', 'C', 'D'

    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal marks = BigDecimal.valueOf(1.00);

    @Column(name = "negative_marks", nullable = false, precision = 5, scale = 2)
    private BigDecimal negativeMarks = BigDecimal.valueOf(0.00);

    public Question() {
    }

    public Question(TestSeries test, Integer orderNo, String text, String optionA, String optionB,
                    String optionC, String optionD, String correctOption, BigDecimal marks, BigDecimal negativeMarks) {
        this.test = test;
        this.orderNo = orderNo;
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.marks = marks != null ? marks : BigDecimal.valueOf(1.00);
        this.negativeMarks = negativeMarks != null ? negativeMarks : BigDecimal.valueOf(0.00);
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

    public Integer getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(Integer orderNo) {
        this.orderNo = orderNo;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getOptionA() {
        return optionA;
    }

    public void setOptionA(String optionA) {
        this.optionA = optionA;
    }

    public String getOptionB() {
        return optionB;
    }

    public void setOptionB(String optionB) {
        this.optionB = optionB;
    }

    public String getOptionC() {
        return optionC;
    }

    public void setOptionC(String optionC) {
        this.optionC = optionC;
    }

    public String getOptionD() {
        return optionD;
    }

    public void setOptionD(String optionD) {
        this.optionD = optionD;
    }

    public String getCorrectOption() {
        return correctOption;
    }

    public void setCorrectOption(String correctOption) {
        this.correctOption = correctOption;
    }

    public BigDecimal getMarks() {
        return marks;
    }

    public void setMarks(BigDecimal marks) {
        this.marks = marks;
    }

    public BigDecimal getNegativeMarks() {
        return negativeMarks;
    }

    public void setNegativeMarks(BigDecimal negativeMarks) {
        this.negativeMarks = negativeMarks;
    }
}
