package com.crevolab.testseries;

import com.crevolab.testseries.domain.Question;
import com.crevolab.testseries.domain.TestSeries;
import com.crevolab.testseries.domain.enums.TestStatus;
import com.crevolab.testseries.dto.admin.ParseResultDto;
import com.crevolab.testseries.exception.ResourceNotFoundException;
import com.crevolab.testseries.repository.AttemptRepository;
import com.crevolab.testseries.repository.QuestionRepository;
import com.crevolab.testseries.repository.TestSeriesRepository;
import com.crevolab.testseries.service.AdminTestSeriesService;
import com.crevolab.testseries.service.PdfTextExtractionService;
import com.crevolab.testseries.service.QuestionParserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminTestSeriesServiceParseTest {

    @Mock
    private TestSeriesRepository testSeriesRepository;

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private PdfTextExtractionService pdfTextExtractionService;

    @Mock
    private QuestionRepository questionRepository;

    @Spy
    private QuestionParserService questionParserService = new QuestionParserService();

    @InjectMocks
    private AdminTestSeriesService adminTestSeriesService;

    private TestSeries test;

    @BeforeEach
    void setUp() {
        test = new TestSeries();
        test.setId(10L);
        test.setTitle("Mock Physics Test");
        test.setStatus(TestStatus.DRAFT);
    }

    @Test
    @DisplayName("parseAndSaveQuestions saves questions with correct_option=NULL, marks=4, negativeMarks=1, test stays DRAFT")
    @SuppressWarnings("unchecked")
    void testParseAndSaveQuestionsSetsDefaultsCorrectly() {
        given(testSeriesRepository.findById(10L)).willReturn(Optional.of(test));

        String rawText = """
                1. What is the acceleration due to gravity?
                A. 9.8 m/s^2
                B. 8.9 m/s^2
                C. 10 m/s^2
                D. 12 m/s^2
                
                2. Which is a scalar quantity?
                A. Velocity
                B. Speed
                C. Force
                D. Momentum
                """;

        ParseResultDto result = adminTestSeriesService.parseAndSaveQuestions(10L, rawText);

        assertThat(result.getQuestionsCreated()).isEqualTo(2);
        assertThat(result.getWarnings()).isEmpty();

        // Verify deleteByTestId called for idempotency
        verify(questionRepository).deleteByTestId(10L);

        // Verify saved questions
        ArgumentCaptor<List<Question>> captor = ArgumentCaptor.forClass(List.class);
        verify(questionRepository).saveAll(captor.capture());

        List<Question> savedQuestions = captor.getValue();
        assertThat(savedQuestions).hasSize(2);

        Question q1 = savedQuestions.get(0);
        assertThat(q1.getOrderNo()).isEqualTo(1);
        assertThat(q1.getText()).isEqualTo("What is the acceleration due to gravity?");
        assertThat(q1.getOptionA()).isEqualTo("9.8 m/s^2");
        assertThat(q1.getCorrectOption()).isNull(); // Strictly NULL per requirement
        assertThat(q1.getMarks()).isEqualByComparingTo(BigDecimal.valueOf(4.00)); // Default 4
        assertThat(q1.getNegativeMarks()).isEqualByComparingTo(BigDecimal.valueOf(1.00)); // Default 1

        Question q2 = savedQuestions.get(1);
        assertThat(q2.getOrderNo()).isEqualTo(2);
        assertThat(q2.getText()).isEqualTo("Which is a scalar quantity?");
        assertThat(q2.getOptionB()).isEqualTo("Speed");
        assertThat(q2.getCorrectOption()).isNull();
        assertThat(q2.getMarks()).isEqualByComparingTo(BigDecimal.valueOf(4.00));
        assertThat(q2.getNegativeMarks()).isEqualByComparingTo(BigDecimal.valueOf(1.00));

        // Verify test status stays DRAFT
        verify(testSeriesRepository).save(test);
        assertThat(test.getStatus()).isEqualTo(TestStatus.DRAFT);
    }

    @Test
    @DisplayName("parseAndSaveQuestions returns warnings for missing options or text")
    void testParseAndSaveQuestionsReturnsWarnings() {
        given(testSeriesRepository.findById(10L)).willReturn(Optional.of(test));

        String rawText = """
                1. Question with only two options
                A. Option 1
                B. Option 2
                """;

        ParseResultDto result = adminTestSeriesService.parseAndSaveQuestions(10L, rawText);

        assertThat(result.getQuestionsCreated()).isEqualTo(1);
        assertThat(result.getWarnings()).hasSize(1);
        assertThat(result.getWarnings().get(0)).contains("Question 1").contains("fewer than 4 options");
    }

    @Test
    @DisplayName("parseAndSaveQuestions throws ResourceNotFoundException if test not found")
    void testParseAndSaveQuestionsNotFound() {
        given(testSeriesRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> adminTestSeriesService.parseAndSaveQuestions(999L, "some text"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Test not found with id: 999");
    }

    @Test
    @DisplayName("parseAndSaveQuestions throws IllegalArgumentException if no PDF and no rawText")
    void testParseAndSaveQuestionsNoPdf() {
        test.setSourcePdfPath(null);
        given(testSeriesRepository.findById(10L)).willReturn(Optional.of(test));

        assertThatThrownBy(() -> adminTestSeriesService.parseAndSaveQuestions(10L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("No PDF uploaded for test id: 10");
    }
}
