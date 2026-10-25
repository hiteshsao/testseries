package com.crevolab.testseries;

import com.crevolab.testseries.domain.Question;
import com.crevolab.testseries.domain.TestSeries;
import com.crevolab.testseries.dto.admin.QuestionRequestDto;
import com.crevolab.testseries.dto.admin.QuestionResponseDto;
import com.crevolab.testseries.exception.ResourceNotFoundException;
import com.crevolab.testseries.repository.QuestionRepository;
import com.crevolab.testseries.repository.TestSeriesRepository;
import com.crevolab.testseries.service.AdminQuestionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class AdminQuestionServiceTest {

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private TestSeriesRepository testSeriesRepository;

    @InjectMocks
    private AdminQuestionService adminQuestionService;

    private TestSeries test;

    @BeforeEach
    void setUp() {
        test = new TestSeries();
        test.setId(5L);
        test.setTitle("Chemistry Mock Test");
    }

    @Test
    @DisplayName("createQuestion appends question at the end when existing questions exist")
    void testCreateQuestionAppendsAtEnd() {
        given(testSeriesRepository.findById(5L)).willReturn(Optional.of(test));
        given(questionRepository.findMaxOrderNoByTestId(5L)).willReturn(3);

        QuestionRequestDto request = new QuestionRequestDto(
                "What is the pH of pure water?",
                "5", "7", "9", "14",
                "B",
                BigDecimal.valueOf(4.00),
                BigDecimal.valueOf(1.00)
        );

        given(questionRepository.save(any(Question.class))).willAnswer(inv -> {
            Question q = inv.getArgument(0);
            q.setId(101L);
            return q;
        });

        QuestionResponseDto response = adminQuestionService.createQuestion(5L, request);

        assertThat(response.getId()).isEqualTo(101L);
        assertThat(response.getOrderNo()).isEqualTo(4); // max(3) + 1 = 4
        assertThat(response.getText()).isEqualTo("What is the pH of pure water?");
        assertThat(response.getOptionA()).isEqualTo("5");
        assertThat(response.getOptionB()).isEqualTo("7");
        assertThat(response.getCorrectOption()).isEqualTo("B");
        assertThat(response.getMarks()).isEqualByComparingTo(BigDecimal.valueOf(4.00));
        assertThat(response.getNegativeMarks()).isEqualByComparingTo(BigDecimal.valueOf(1.00));
    }

    @Test
    @DisplayName("createQuestion sets orderNo = 1 when test has no existing questions")
    void testCreateQuestionFirstQuestion() {
        given(testSeriesRepository.findById(5L)).willReturn(Optional.of(test));
        given(questionRepository.findMaxOrderNoByTestId(5L)).willReturn(0);

        QuestionRequestDto request = new QuestionRequestDto(
                "Initial Question",
                "A", "B", "C", "D",
                null,
                BigDecimal.valueOf(2.00),
                BigDecimal.valueOf(0.50)
        );

        given(questionRepository.save(any(Question.class))).willAnswer(inv -> {
            Question q = inv.getArgument(0);
            q.setId(1L);
            return q;
        });

        QuestionResponseDto response = adminQuestionService.createQuestion(5L, request);

        assertThat(response.getOrderNo()).isEqualTo(1);
        assertThat(response.getCorrectOption()).isNull();
    }

    @Test
    @DisplayName("deleteQuestion renumbers order_no so there are no gaps after deletion")
    @SuppressWarnings("unchecked")
    void testDeleteQuestionRenumbersWithoutGaps() {
        Question q1 = new Question(test, 1, "Q1", "A", "B", "C", "D", "A", BigDecimal.valueOf(4), BigDecimal.valueOf(1));
        q1.setId(10L);
        Question q2 = new Question(test, 2, "Q2", "A", "B", "C", "D", "B", BigDecimal.valueOf(4), BigDecimal.valueOf(1));
        q2.setId(20L);
        Question q3 = new Question(test, 3, "Q3", "A", "B", "C", "D", "C", BigDecimal.valueOf(4), BigDecimal.valueOf(1));
        q3.setId(30L);
        Question q4 = new Question(test, 4, "Q4", "A", "B", "C", "D", "D", BigDecimal.valueOf(4), BigDecimal.valueOf(1));
        q4.setId(40L);

        // Delete Q2 (orderNo 2)
        given(questionRepository.findById(20L)).willReturn(Optional.of(q2));

        // Remaining after delete of Q2: Q1, Q3, Q4
        List<Question> remaining = new ArrayList<>(List.of(q1, q3, q4));
        given(questionRepository.findByTestIdOrderByOrderNoAsc(5L)).willReturn(remaining);

        adminQuestionService.deleteQuestion(20L);

        verify(questionRepository).delete(q2);
        verify(questionRepository).flush();

        ArgumentCaptor<List<Question>> captor = ArgumentCaptor.forClass(List.class);
        verify(questionRepository).saveAll(captor.capture());

        List<Question> renumbered = captor.getValue();
        assertThat(renumbered).hasSize(3);
        assertThat(renumbered.get(0).getId()).isEqualTo(10L);
        assertThat(renumbered.get(0).getOrderNo()).isEqualTo(1);

        assertThat(renumbered.get(1).getId()).isEqualTo(30L);
        assertThat(renumbered.get(1).getOrderNo()).isEqualTo(2); // Was 3, renumbered to 2

        assertThat(renumbered.get(2).getId()).isEqualTo(40L);
        assertThat(renumbered.get(2).getOrderNo()).isEqualTo(3); // Was 4, renumbered to 3
    }

    @Test
    @DisplayName("updateQuestion updates all mutable fields and retains orderNo")
    void testUpdateQuestion() {
        Question existing = new Question(test, 2, "Old text", "A", "B", "C", "D", "A", BigDecimal.valueOf(2), BigDecimal.valueOf(0));
        existing.setId(50L);

        given(questionRepository.findById(50L)).willReturn(Optional.of(existing));
        given(questionRepository.save(any(Question.class))).willAnswer(inv -> inv.getArgument(0));

        QuestionRequestDto updateReq = new QuestionRequestDto(
                "Updated text",
                "New A", "New B", "New C", "New D",
                "c", // lowercase should be normalized to uppercase 'C'
                BigDecimal.valueOf(4.00),
                BigDecimal.valueOf(1.00)
        );

        QuestionResponseDto updated = adminQuestionService.updateQuestion(50L, updateReq);

        assertThat(updated.getId()).isEqualTo(50L);
        assertThat(updated.getOrderNo()).isEqualTo(2); // Retains original orderNo
        assertThat(updated.getText()).isEqualTo("Updated text");
        assertThat(updated.getOptionA()).isEqualTo("New A");
        assertThat(updated.getCorrectOption()).isEqualTo("C");
        assertThat(updated.getMarks()).isEqualByComparingTo(BigDecimal.valueOf(4.00));
        assertThat(updated.getNegativeMarks()).isEqualByComparingTo(BigDecimal.valueOf(1.00));
    }

    @Test
    @DisplayName("getQuestionsByTestId returns ordered questions")
    void testGetQuestionsByTestId() {
        given(testSeriesRepository.existsById(5L)).willReturn(true);

        Question q1 = new Question(test, 1, "Q1", "A", "B", "C", "D", "A", BigDecimal.valueOf(4), BigDecimal.valueOf(1));
        q1.setId(1L);
        Question q2 = new Question(test, 2, "Q2", "A", "B", "C", "D", "B", BigDecimal.valueOf(4), BigDecimal.valueOf(1));
        q2.setId(2L);

        given(questionRepository.findByTestIdOrderByOrderNoAsc(5L)).willReturn(List.of(q1, q2));

        List<QuestionResponseDto> result = adminQuestionService.getQuestionsByTestId(5L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getOrderNo()).isEqualTo(1);
        assertThat(result.get(1).getOrderNo()).isEqualTo(2);
    }

    @Test
    @DisplayName("Throws ResourceNotFoundException when test or question not found")
    void testNotFoundExceptions() {
        given(testSeriesRepository.existsById(999L)).willReturn(false);
        assertThatThrownBy(() -> adminQuestionService.getQuestionsByTestId(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        given(testSeriesRepository.findById(999L)).willReturn(Optional.empty());
        QuestionRequestDto req = new QuestionRequestDto("T", "A", "B", "C", "D", "A", BigDecimal.ONE, BigDecimal.ZERO);
        assertThatThrownBy(() -> adminQuestionService.createQuestion(999L, req))
                .isInstanceOf(ResourceNotFoundException.class);

        given(questionRepository.findById(999L)).willReturn(Optional.empty());
        assertThatThrownBy(() -> adminQuestionService.updateQuestion(999L, req))
                .isInstanceOf(ResourceNotFoundException.class);

        assertThatThrownBy(() -> adminQuestionService.deleteQuestion(999L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
