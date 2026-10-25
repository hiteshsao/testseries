package com.crevolab.testseries;

import com.crevolab.testseries.service.QuestionParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class QuestionParserServiceTest {

    private QuestionParserService parserService;

    @BeforeEach
    void setUp() {
        parserService = new QuestionParserService();
    }

    @Test
    @DisplayName("Sample 1 - Normal question: clean question with 4 options A, B, C, D and varied markers")
    void testNormalQuestionParsing() {
        String sampleText = """
                1. What is the capital of France?
                A. Paris
                B. London
                C. Berlin
                D. Madrid
                
                Q2. Which planet is closest to the Sun?
                (A) Venus
                (B) Mercury
                (C) Mars
                (D) Jupiter
                
                3) What is the chemical formula for water?
                A) H2O
                B) CO2
                C) NaCl
                D) O2
                """;

        QuestionParserService.ParseResult result = parserService.parse(sampleText);

        assertThat(result.getQuestions()).hasSize(3);
        assertThat(result.getWarnings()).isEmpty();

        // Question 1
        QuestionParserService.ParsedQuestion q1 = result.getQuestions().get(0);
        assertThat(q1.getOrderNo()).isEqualTo(1);
        assertThat(q1.getQuestionNumberStr()).isEqualTo("1");
        assertThat(q1.getText()).isEqualTo("What is the capital of France?");
        assertThat(q1.getOptionA()).isEqualTo("Paris");
        assertThat(q1.getOptionB()).isEqualTo("London");
        assertThat(q1.getOptionC()).isEqualTo("Berlin");
        assertThat(q1.getOptionD()).isEqualTo("Madrid");

        // Question 2
        QuestionParserService.ParsedQuestion q2 = result.getQuestions().get(1);
        assertThat(q2.getOrderNo()).isEqualTo(2);
        assertThat(q2.getQuestionNumberStr()).isEqualTo("2");
        assertThat(q2.getText()).isEqualTo("Which planet is closest to the Sun?");
        assertThat(q2.getOptionA()).isEqualTo("Venus");
        assertThat(q2.getOptionB()).isEqualTo("Mercury");
        assertThat(q2.getOptionC()).isEqualTo("Mars");
        assertThat(q2.getOptionD()).isEqualTo("Jupiter");

        // Question 3
        QuestionParserService.ParsedQuestion q3 = result.getQuestions().get(2);
        assertThat(q3.getOrderNo()).isEqualTo(3);
        assertThat(q3.getQuestionNumberStr()).isEqualTo("3");
        assertThat(q3.getText()).isEqualTo("What is the chemical formula for water?");
        assertThat(q3.getOptionA()).isEqualTo("H2O");
        assertThat(q3.getOptionB()).isEqualTo("CO2");
        assertThat(q3.getOptionC()).isEqualTo("NaCl");
        assertThat(q3.getOptionD()).isEqualTo("O2");
    }

    @Test
    @DisplayName("Sample 2 - Multi-line question: question prompt and option text span multiple lines in order")
    void testMultiLineQuestionParsing() {
        String sampleText = """
                1. Consider the following algorithm:
                   Initialize count = 0.
                   Loop through elements from 1 to 10.
                   Increment count by i each step.
                   What is the final value of count?
                A. 45
                B. 55
                   which is the sum of integers
                   from 1 through 10.
                C. 100
                D. None of the above
                """;

        QuestionParserService.ParseResult result = parserService.parse(sampleText);

        assertThat(result.getQuestions()).hasSize(1);
        assertThat(result.getWarnings()).isEmpty();

        QuestionParserService.ParsedQuestion q = result.getQuestions().get(0);
        assertThat(q.getOrderNo()).isEqualTo(1);
        assertThat(q.getQuestionNumberStr()).isEqualTo("1");
        assertThat(q.getText()).contains("Consider the following algorithm:");
        assertThat(q.getText()).contains("Initialize count = 0.");
        assertThat(q.getText()).contains("Loop through elements from 1 to 10.");
        assertThat(q.getText()).contains("Increment count by i each step.");
        assertThat(q.getText()).contains("What is the final value of count?");
        assertThat(q.getOptionA()).isEqualTo("45");
        assertThat(q.getOptionB()).isEqualTo("55 which is the sum of integers from 1 through 10.");
        assertThat(q.getOptionC()).isEqualTo("100");
        assertThat(q.getOptionD()).isEqualTo("None of the above");
    }

    @Test
    @DisplayName("Sample 3 - Missing option and missing question text: returns question with warning containing number")
    void testMissingOptionAndMissingQuestionTextParsing() {
        String sampleText = """
                1. Which of the following is a primary color?
                A. Red
                B. Green
                C. Blue
                
                2.
                A. Yes
                B. No
                C. Maybe
                D. Unknown
                
                Q3. Binary search runs in:
                A. O(log n)
                """;

        QuestionParserService.ParseResult result = parserService.parse(sampleText);

        assertThat(result.getQuestions()).hasSize(3);
        List<String> warnings = result.getWarnings();
        assertThat(warnings).hasSize(3);

        // Warning for Question 1: fewer than 4 options (found 3)
        assertThat(warnings.get(0)).contains("Question 1").contains("fewer than 4 options").contains("3");

        // Warning for Question 2: no question text
        assertThat(warnings.get(1)).contains("Question 2").contains("no question text");

        // Warning for Question 3: fewer than 4 options (found 1)
        assertThat(warnings.get(2)).contains("Question 3").contains("fewer than 4 options").contains("1");

        // Verify Question 1 is still returned
        QuestionParserService.ParsedQuestion q1 = result.getQuestions().get(0);
        assertThat(q1.getOptionA()).isEqualTo("Red");
        assertThat(q1.getOptionB()).isEqualTo("Green");
        assertThat(q1.getOptionC()).isEqualTo("Blue");
        assertThat(q1.getOptionD()).isEmpty();

        // Verify Question 2 is still returned
        QuestionParserService.ParsedQuestion q2 = result.getQuestions().get(1);
        assertThat(q2.getText()).isEmpty();
        assertThat(q2.getOptionA()).isEqualTo("Yes");
        assertThat(q2.getOptionD()).isEqualTo("Unknown");

        // Verify Question 3 is still returned
        QuestionParserService.ParsedQuestion q3 = result.getQuestions().get(2);
        assertThat(q3.getOptionA()).isEqualTo("O(log n)");
        assertThat(q3.getOptionB()).isEmpty();
    }

    @Test
    @DisplayName("Sample 4 - Noisy headers and footers: 'Page 2 of 5', '- 2 -', etc. are ignored")
    void testNoisyHeadersParsing() {
        String sampleText = """
                Sample Engineering Entrance Exam
                Page 1 of 5
                
                1. What is the SI unit of electric current?
                A. Volt
                B. Ampere
                Page 1 of 5
                C. Ohm
                D. Watt
                
                - Page 2 of 5 -
                
                2. Acceleration due to gravity at sea level is approximately:
                A. 9.8 m/s^2
                B. 8.9 m/s^2
                C. 10.8 m/s^2
                D. 7.8 m/s^2
                
                - 2 -
                Page 2 of 5
                """;

        QuestionParserService.ParseResult result = parserService.parse(sampleText);

        assertThat(result.getQuestions()).hasSize(2);
        assertThat(result.getWarnings()).isEmpty();

        QuestionParserService.ParsedQuestion q1 = result.getQuestions().get(0);
        assertThat(q1.getText()).isEqualTo("What is the SI unit of electric current?");
        assertThat(q1.getText()).doesNotContain("Page");
        assertThat(q1.getOptionB()).isEqualTo("Ampere");
        assertThat(q1.getOptionC()).isEqualTo("Ohm");
        assertThat(q1.getOptionD()).isEqualTo("Watt");

        QuestionParserService.ParsedQuestion q2 = result.getQuestions().get(1);
        assertThat(q2.getText()).isEqualTo("Acceleration due to gravity at sea level is approximately:");
        assertThat(q2.getText()).doesNotContain("Page");
        assertThat(q2.getOptionA()).isEqualTo("9.8 m/s^2");
        assertThat(q2.getOptionB()).isEqualTo("8.9 m/s^2");
    }

    @Test
    @DisplayName("Edge Case - Inline options on same line e.g. '(A) 1  (B) 2  (C) 3  (D) 4'")
    void testInlineOptionsParsing() {
        String sampleText = """
                1. What is 2 + 2?
                (A) 3  (B) 4  (C) 5  (D) 6
                """;

        QuestionParserService.ParseResult result = parserService.parse(sampleText);

        assertThat(result.getQuestions()).hasSize(1);
        assertThat(result.getWarnings()).isEmpty();

        QuestionParserService.ParsedQuestion q = result.getQuestions().get(0);
        assertThat(q.getText()).isEqualTo("What is 2 + 2?");
        assertThat(q.getOptionA()).isEqualTo("3");
        assertThat(q.getOptionB()).isEqualTo("4");
        assertThat(q.getOptionC()).isEqualTo("5");
        assertThat(q.getOptionD()).isEqualTo("6");
    }

    @Test
    @DisplayName("Edge Case - Empty and blank text")
    void testEmptyAndBlankText() {
        QuestionParserService.ParseResult result = parserService.parse("");
        assertThat(result.getQuestions()).isEmpty();
        assertThat(result.getWarnings()).isEmpty();

        result = parserService.parse(null);
        assertThat(result.getQuestions()).isEmpty();
        assertThat(result.getWarnings()).isEmpty();
    }
}
