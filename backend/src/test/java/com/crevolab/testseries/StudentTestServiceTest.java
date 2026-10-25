package com.crevolab.testseries;

import com.crevolab.testseries.domain.Attempt;
import com.crevolab.testseries.domain.AttemptAnswer;
import com.crevolab.testseries.domain.AttemptAnswerId;
import com.crevolab.testseries.domain.Question;
import com.crevolab.testseries.domain.TestSeries;
import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.domain.enums.AttemptStatus;
import com.crevolab.testseries.domain.enums.TestStatus;
import com.crevolab.testseries.dto.student.StudentAnswerRequestDto;
import com.crevolab.testseries.dto.student.StudentAnswerResponseDto;
import com.crevolab.testseries.dto.student.StudentAttemptResponseDto;
import com.crevolab.testseries.dto.student.StudentQuestionDto;
import com.crevolab.testseries.dto.student.StudentTestSummaryDto;
import com.crevolab.testseries.repository.AttemptAnswerRepository;
import com.crevolab.testseries.repository.AttemptRepository;
import com.crevolab.testseries.repository.PurchaseRepository;
import com.crevolab.testseries.repository.QuestionRepository;
import com.crevolab.testseries.repository.TestSeriesRepository;
import com.crevolab.testseries.service.StudentTestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StudentTestServiceTest {

    @Mock
    private TestSeriesRepository testSeriesRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AttemptRepository attemptRepository;

    @Mock
    private AttemptAnswerRepository attemptAnswerRepository;

    @Mock
    private PurchaseRepository purchaseRepository;

    @InjectMocks
    private StudentTestService studentTestService;

    private TestSeries publishedTest;
    private Question sampleQuestion;

    @BeforeEach
    void setUp() {
        publishedTest = new TestSeries();
        publishedTest.setId(100L);
        publishedTest.setTitle("JEE Advanced Mock");
        publishedTest.setDurationMinutes(60);
        publishedTest.setStatus(TestStatus.PUBLISHED);
        publishedTest.setAccessType(AccessType.FREE);
        publishedTest.setPrice(BigDecimal.ZERO);
        publishedTest.setMaxAttempts(2);

        sampleQuestion = new Question();
        sampleQuestion.setId(501L);
        sampleQuestion.setTest(publishedTest);
        sampleQuestion.setOrderNo(1);
        sampleQuestion.setText("What is the speed of light?");
        sampleQuestion.setOptionA("3x10^8 m/s");
        sampleQuestion.setOptionB("3x10^6 m/s");
        sampleQuestion.setOptionC("3x10^10 m/s");
        sampleQuestion.setOptionD("3x10^5 m/s");
        sampleQuestion.setCorrectOption("A"); // Secret answer key
        sampleQuestion.setMarks(BigDecimal.valueOf(4.0));
        sampleQuestion.setNegativeMarks(BigDecimal.valueOf(1.0));
    }

    @Test
    @DisplayName("getPublishedTests returns only tests with PUBLISHED status")
    void testGetPublishedTests() {
        given(testSeriesRepository.findByStatus(TestStatus.PUBLISHED)).willReturn(List.of(publishedTest));
        given(attemptRepository.countByTestIdAndStudentId(100L, "student-1")).willReturn(0L);
        given(questionRepository.findByTestIdOrderByOrderNoAsc(100L)).willReturn(List.of(sampleQuestion));

        List<StudentTestSummaryDto> tests = studentTestService.getPublishedTests("student-1");

        assertThat(tests).hasSize(1);
        assertThat(tests.get(0).getId()).isEqualTo(100L);
        assertThat(tests.get(0).getStatus()).isEqualTo(TestStatus.PUBLISHED);
        assertThat(tests.get(0).getPurchased()).isTrue();
        assertThat(tests.get(0).getCanAttempt()).isTrue();
    }

    @Test
    @DisplayName("startTest fails if test is not published")
    void testStartTestFailsWhenNotPublished() {
        publishedTest.setStatus(TestStatus.DRAFT);
        given(testSeriesRepository.findById(100L)).willReturn(Optional.of(publishedTest));

        assertThatThrownBy(() -> studentTestService.startTest(100L, "student-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("not published");
    }

    @Test
    @DisplayName("startTest fails if test is PAID and student has not purchased it")
    void testStartTestFailsWhenPaidAndNotPurchased() {
        publishedTest.setAccessType(AccessType.PAID);
        publishedTest.setPrice(BigDecimal.valueOf(499.0));

        given(testSeriesRepository.findById(100L)).willReturn(Optional.of(publishedTest));
        given(purchaseRepository.existsByTestIdAndStudentId(100L, "student-1")).willReturn(false);

        assertThatThrownBy(() -> studentTestService.startTest(100L, "student-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("paid test");
    }

    @Test
    @DisplayName("startTest fails when max attempts is reached")
    void testStartTestFailsWhenMaxAttemptsReached() {
        publishedTest.setMaxAttempts(1);

        given(testSeriesRepository.findById(100L)).willReturn(Optional.of(publishedTest));
        given(attemptRepository.countByTestIdAndStudentId(100L, "student-1")).willReturn(1L);

        assertThatThrownBy(() -> studentTestService.startTest(100L, "student-1"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Maximum attempts");
    }

    @Test
    @DisplayName("startTest creates attempt with ends_at = now + duration and returns questions without correctOption")
    void testStartTestSucceeds() {
        given(testSeriesRepository.findById(100L)).willReturn(Optional.of(publishedTest));
        given(attemptRepository.countByTestIdAndStudentId(100L, "student-1")).willReturn(0L);
        given(questionRepository.findByTestIdOrderByOrderNoAsc(100L)).willReturn(List.of(sampleQuestion));
        given(attemptRepository.save(any(Attempt.class))).willAnswer(invocation -> {
            Attempt a = invocation.getArgument(0);
            a.setId(999L);
            return a;
        });

        Instant beforeStart = Instant.now().minusSeconds(1);
        StudentAttemptResponseDto response = studentTestService.startTest(100L, "student-1");
        Instant afterStart = Instant.now().plusSeconds(1);

        assertThat(response).isNotNull();
        assertThat(response.getAttemptId()).isEqualTo(999L);
        assertThat(response.getDurationMinutes()).isEqualTo(60);

        // Verify ends_at = startedAt + 60 minutes
        Duration attemptDuration = Duration.between(response.getStartedAt(), response.getEndsAt());
        assertThat(attemptDuration.toMinutes()).isEqualTo(60);
        assertThat(response.getStartedAt()).isBetween(beforeStart, afterStart);

        // Security check: questions must be present, with marks only
        assertThat(response.getQuestions()).hasSize(1);
        StudentQuestionDto q = response.getQuestions().get(0);
        assertThat(q.getId()).isEqualTo(501L);
        assertThat(q.getMarks()).isEqualByComparingTo("4.0");
        assertThat(q.getNegativeMarks()).isEqualByComparingTo("1.0");
    }

    @Test
    @DisplayName("saveAnswer rejects when attempt has already been submitted")
    void testSaveAnswerRejectsWhenSubmitted() {
        Attempt attempt = new Attempt();
        attempt.setId(999L);
        attempt.setTest(publishedTest);
        attempt.setStudentId("student-1");
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setEndsAt(Instant.now().plusSeconds(3600));

        given(attemptRepository.findById(999L)).willReturn(Optional.of(attempt));

        StudentAnswerRequestDto req = new StudentAnswerRequestDto(501L, "A", false);

        assertThatThrownBy(() -> studentTestService.saveAnswer(999L, "student-1", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("already been submitted");
    }

    @Test
    @DisplayName("saveAnswer rejects after ends_at timestamp has passed")
    void testSaveAnswerRejectsAfterEndsAt() {
        Attempt attempt = new Attempt();
        attempt.setId(999L);
        attempt.setTest(publishedTest);
        attempt.setStudentId("student-1");
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        // Time expired 5 minutes ago
        attempt.setEndsAt(Instant.now().minus(Duration.ofMinutes(5)));

        given(attemptRepository.findById(999L)).willReturn(Optional.of(attempt));

        StudentAnswerRequestDto req = new StudentAnswerRequestDto(501L, "B", false);

        assertThatThrownBy(() -> studentTestService.saveAnswer(999L, "student-1", req))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("time limit has expired");
    }

    @Test
    @DisplayName("saveAnswer saves option and marked_for_review during active window")
    void testSaveAnswerSucceeds() {
        Attempt attempt = new Attempt();
        attempt.setId(999L);
        attempt.setTest(publishedTest);
        attempt.setStudentId("student-1");
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setEndsAt(Instant.now().plus(Duration.ofMinutes(30)));

        given(attemptRepository.findById(999L)).willReturn(Optional.of(attempt));
        given(questionRepository.findById(501L)).willReturn(Optional.of(sampleQuestion));
        given(attemptAnswerRepository.findById(new AttemptAnswerId(999L, 501L))).willReturn(Optional.empty());

        StudentAnswerRequestDto req = new StudentAnswerRequestDto(501L, "c", true);

        StudentAnswerResponseDto res = studentTestService.saveAnswer(999L, "student-1", req);

        assertThat(res.getQuestionId()).isEqualTo(501L);
        assertThat(res.getSelectedOption()).isEqualTo("C");
        assertThat(res.getMarkedForReview()).isTrue();
        assertThat(res.getRemainingSeconds()).isGreaterThan(0);

        ArgumentCaptor<AttemptAnswer> captor = ArgumentCaptor.forClass(AttemptAnswer.class);
        verify(attemptAnswerRepository).save(captor.capture());
        assertThat(captor.getValue().getSelectedOption()).isEqualTo("C");
        assertThat(captor.getValue().getMarkedForReview()).isTrue();
    }

    @Test
    @DisplayName("getAttempt resumes attempt returning saved answers and remaining seconds")
    void testGetAttemptResumesCorrectly() {
        Attempt attempt = new Attempt();
        attempt.setId(999L);
        attempt.setTest(publishedTest);
        attempt.setStudentId("student-1");
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setStartedAt(Instant.now().minus(Duration.ofMinutes(10)));
        attempt.setEndsAt(Instant.now().plus(Duration.ofMinutes(50)));

        AttemptAnswer answer = new AttemptAnswer(attempt, sampleQuestion, "B", true);

        given(attemptRepository.findById(999L)).willReturn(Optional.of(attempt));
        given(attemptAnswerRepository.findAnswersByAttemptId(999L)).willReturn(List.of(answer));
        given(questionRepository.findByTestIdOrderByOrderNoAsc(100L)).willReturn(List.of(sampleQuestion));

        StudentAttemptResponseDto response = studentTestService.getAttempt(999L, "student-1");

        assertThat(response).isNotNull();
        assertThat(response.getAttemptId()).isEqualTo(999L);
        assertThat(response.getRemainingSeconds()).isGreaterThan(2900L);
        assertThat(response.getAnswers()).hasSize(1);
        assertThat(response.getAnswers().get(0).getSelectedOption()).isEqualTo("B");
        assertThat(response.getAnswers().get(0).getMarkedForReview()).isTrue();
    }
}
