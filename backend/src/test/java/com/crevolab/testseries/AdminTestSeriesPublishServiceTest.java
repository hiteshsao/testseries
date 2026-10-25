package com.crevolab.testseries;

import com.crevolab.testseries.domain.Question;
import com.crevolab.testseries.domain.TestSeries;
import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.domain.enums.TestStatus;
import com.crevolab.testseries.dto.admin.TestResponseDto;
import com.crevolab.testseries.exception.TestPublishValidationException;
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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminTestSeriesPublishServiceTest {

    @Mock
    private TestSeriesRepository testSeriesRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private PdfTextExtractionService pdfTextExtractionService;

    @Mock
    private QuestionParserService questionParserService;

    @InjectMocks
    private AdminTestSeriesService adminTestSeriesService;

    private TestSeries validTest;

    @BeforeEach
    void setUp() {
        validTest = new TestSeries();
        validTest.setId(10L);
        validTest.setTitle("JEE Advanced Physics");
        validTest.setDurationMinutes(180);
        validTest.setAccessType(AccessType.FREE);
        validTest.setPrice(BigDecimal.ZERO);
        validTest.setStatus(TestStatus.DRAFT);
    }

    private Question createQuestion(Long id, int orderNo, String correctOption) {
        Question q = new Question();
        q.setId(id);
        q.setTest(validTest);
        q.setOrderNo(orderNo);
        q.setText("Question " + orderNo);
        q.setOptionA("A");
        q.setOptionB("B");
        q.setOptionC("C");
        q.setOptionD("D");
        q.setCorrectOption(correctOption);
        q.setMarks(BigDecimal.valueOf(4.0));
        q.setNegativeMarks(BigDecimal.valueOf(1.0));
        return q;
    }

    @Test
    @DisplayName("Publish fails when test has no questions")
    void testPublishFailsWhenNoQuestions() {
        given(testSeriesRepository.findById(10L)).willReturn(Optional.of(validTest));
        given(questionRepository.findByTestIdOrderByOrderNoAsc(10L)).willReturn(List.of());

        assertThatThrownBy(() -> adminTestSeriesService.publishTest(10L))
                .isInstanceOf(TestPublishValidationException.class)
                .satisfies(ex -> {
                    TestPublishValidationException te = (TestPublishValidationException) ex;
                    assertThat(te.getProblems()).anyMatch(p -> p.toLowerCase().contains("no question") || p.toLowerCase().contains("at least one question"));
                });
    }

    @Test
    @DisplayName("Publish fails when any question has no correct answer")
    void testPublishFailsWhenQuestionHasNoCorrectAnswer() {
        Question q1 = createQuestion(1L, 1, "A");
        Question q2 = createQuestion(2L, 2, null);
        Question q3 = createQuestion(3L, 3, " ");

        given(testSeriesRepository.findById(10L)).willReturn(Optional.of(validTest));
        given(questionRepository.findByTestIdOrderByOrderNoAsc(10L)).willReturn(List.of(q1, q2, q3));

        assertThatThrownBy(() -> adminTestSeriesService.publishTest(10L))
                .isInstanceOf(TestPublishValidationException.class)
                .satisfies(ex -> {
                    TestPublishValidationException te = (TestPublishValidationException) ex;
                    assertThat(te.getProblems()).hasSize(2);
                    assertThat(te.getProblems()).contains("Question 2 has no correct answer");
                    assertThat(te.getProblems()).contains("Question 3 has no correct answer");
                });
    }

    @Test
    @DisplayName("Publish fails when duration is 0 or negative")
    void testPublishFailsWhenDurationZeroOrNegative() {
        validTest.setDurationMinutes(0);

        Question q1 = createQuestion(1L, 1, "B");
        given(testSeriesRepository.findById(10L)).willReturn(Optional.of(validTest));
        given(questionRepository.findByTestIdOrderByOrderNoAsc(10L)).willReturn(List.of(q1));

        assertThatThrownBy(() -> adminTestSeriesService.publishTest(10L))
                .isInstanceOf(TestPublishValidationException.class)
                .satisfies(ex -> {
                    TestPublishValidationException te = (TestPublishValidationException) ex;
                    assertThat(te.getProblems()).anyMatch(p -> p.toLowerCase().contains("duration"));
                });
    }

    @Test
    @DisplayName("Publish fails when access type is PAID but price is <= 0")
    void testPublishFailsWhenPaidAndPriceZeroOrNegative() {
        validTest.setAccessType(AccessType.PAID);
        validTest.setPrice(BigDecimal.ZERO);

        Question q1 = createQuestion(1L, 1, "C");
        given(testSeriesRepository.findById(10L)).willReturn(Optional.of(validTest));
        given(questionRepository.findByTestIdOrderByOrderNoAsc(10L)).willReturn(List.of(q1));

        assertThatThrownBy(() -> adminTestSeriesService.publishTest(10L))
                .isInstanceOf(TestPublishValidationException.class)
                .satisfies(ex -> {
                    TestPublishValidationException te = (TestPublishValidationException) ex;
                    assertThat(te.getProblems()).anyMatch(p -> p.toLowerCase().contains("price") || p.toLowerCase().contains("paid"));
                });
    }

    @Test
    @DisplayName("Publish fails with all accumulated problems when multiple rules fail")
    void testPublishFailsWithAllAccumulatedProblems() {
        validTest.setDurationMinutes(-5);
        validTest.setAccessType(AccessType.PAID);
        validTest.setPrice(BigDecimal.valueOf(-10));

        given(testSeriesRepository.findById(10L)).willReturn(Optional.of(validTest));
        given(questionRepository.findByTestIdOrderByOrderNoAsc(10L)).willReturn(List.of());

        assertThatThrownBy(() -> adminTestSeriesService.publishTest(10L))
                .isInstanceOf(TestPublishValidationException.class)
                .satisfies(ex -> {
                    TestPublishValidationException te = (TestPublishValidationException) ex;
                    assertThat(te.getProblems()).hasSize(3);
                    assertThat(te.getProblems()).anyMatch(p -> p.contains("duration"));
                    assertThat(te.getProblems()).anyMatch(p -> p.contains("Paid test"));
                    assertThat(te.getProblems()).anyMatch(p -> p.contains("question"));
                });
    }

    @Test
    @DisplayName("Publish succeeds when all rules pass")
    void testPublishSucceedsWhenValid() {
        Question q1 = createQuestion(1L, 1, "A");
        Question q2 = createQuestion(2L, 2, "D");

        given(testSeriesRepository.findById(10L)).willReturn(Optional.of(validTest));
        given(questionRepository.findByTestIdOrderByOrderNoAsc(10L)).willReturn(List.of(q1, q2));
        given(testSeriesRepository.save(any(TestSeries.class))).willAnswer(invocation -> invocation.getArgument(0));

        TestResponseDto result = adminTestSeriesService.publishTest(10L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TestStatus.PUBLISHED);
        verify(testSeriesRepository).save(validTest);
        assertThat(validTest.getStatus()).isEqualTo(TestStatus.PUBLISHED);
    }

    @Test
    @DisplayName("Unpublish succeeds and sets status to DRAFT")
    void testUnpublishSucceeds() {
        validTest.setStatus(TestStatus.PUBLISHED);

        given(testSeriesRepository.findById(10L)).willReturn(Optional.of(validTest));
        given(testSeriesRepository.save(any(TestSeries.class))).willAnswer(invocation -> invocation.getArgument(0));

        TestResponseDto result = adminTestSeriesService.unpublishTest(10L);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(TestStatus.DRAFT);
        verify(testSeriesRepository).save(validTest);
        assertThat(validTest.getStatus()).isEqualTo(TestStatus.DRAFT);
    }
}
