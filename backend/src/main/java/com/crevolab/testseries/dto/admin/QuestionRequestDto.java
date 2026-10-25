package com.crevolab.testseries.dto.admin;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public class QuestionRequestDto {

    @NotBlank(message = "Question text is required")
    private String text;

    @NotBlank(message = "Option A is required")
    private String optionA;

    @NotBlank(message = "Option B is required")
    private String optionB;

    @NotBlank(message = "Option C is required")
    private String optionC;

    @NotBlank(message = "Option D is required")
    private String optionD;

    @Pattern(regexp = "^$|^[A-Da-d]$", message = "Correct option must be A, B, C, D or null")
    private String correctOption;

    @NotNull(message = "Marks is required")
    @DecimalMin(value = "0.01", inclusive = true, message = "Marks must be greater than 0")
    private BigDecimal marks;

    @NotNull(message = "Negative marks is required")
    @DecimalMin(value = "0.00", inclusive = true, message = "Negative marks must be greater than or equal to 0")
    private BigDecimal negativeMarks;

    public QuestionRequestDto() {
    }

    public QuestionRequestDto(String text, String optionA, String optionB, String optionC, String optionD,
                              String correctOption, BigDecimal marks, BigDecimal negativeMarks) {
        this.text = text;
        this.optionA = optionA;
        this.optionB = optionB;
        this.optionC = optionC;
        this.optionD = optionD;
        this.correctOption = correctOption;
        this.marks = marks;
        this.negativeMarks = negativeMarks;
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
