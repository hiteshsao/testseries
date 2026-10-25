package com.crevolab.testseries.service;

import com.crevolab.testseries.domain.Attempt;
import com.crevolab.testseries.domain.AttemptAnswer;
import com.crevolab.testseries.domain.AttemptAnswerId;
import com.crevolab.testseries.domain.Question;
import com.crevolab.testseries.domain.TestSeries;
import com.crevolab.testseries.domain.enums.AccessType;
import com.crevolab.testseries.domain.enums.AttemptStatus;
import com.crevolab.testseries.domain.enums.TestStatus;
import com.crevolab.testseries.dto.student.StudentAnswerDto;
import com.crevolab.testseries.dto.student.StudentAnswerRequestDto;
import com.crevolab.testseries.dto.student.StudentAnswerResponseDto;
import com.crevolab.testseries.dto.student.StudentAttemptResponseDto;
import com.crevolab.testseries.dto.student.StudentQuestionDto;
import com.crevolab.testseries.dto.student.StudentSubmitResultDto;
import com.crevolab.testseries.dto.student.StudentTestSummaryDto;
import com.crevolab.testseries.exception.ConflictException;
import com.crevolab.testseries.exception.ForbiddenOperationException;
import com.crevolab.testseries.exception.ResourceNotFoundException;
import com.crevolab.testseries.repository.AttemptAnswerRepository;
import com.crevolab.testseries.repository.AttemptRepository;
import com.crevolab.testseries.repository.PurchaseRepository;
import com.crevolab.testseries.repository.QuestionRepository;
import com.crevolab.testseries.repository.TestSeriesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class StudentTestService {

    private final TestSeriesRepository testSeriesRepository;
    private final QuestionRepository questionRepository;
    private final AttemptRepository attemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final PurchaseRepository purchaseRepository;

    public StudentTestService(TestSeriesRepository testSeriesRepository,
                              QuestionRepository questionRepository,
                              AttemptRepository attemptRepository,
                              AttemptAnswerRepository attemptAnswerRepository,
                              PurchaseRepository purchaseRepository) {
        this.testSeriesRepository = testSeriesRepository;
        this.questionRepository = questionRepository;
        this.attemptRepository = attemptRepository;
        this.attemptAnswerRepository = attemptAnswerRepository;
        this.purchaseRepository = purchaseRepository;
    }

    /**
     * GET /api/student/tests (PUBLISHED only)
     */
    @Transactional(readOnly = true)
    public List<StudentTestSummaryDto> getPublishedTests(String studentId) {
        List<TestSeries> publishedTests = testSeriesRepository.findByStatus(TestStatus.PUBLISHED);

        return publishedTests.stream().map(test -> {
            long attemptsUsed = attemptRepository.countByTestIdAndStudentId(test.getId(), studentId);
            boolean isFree = test.getAccessType() == AccessType.FREE;
            boolean purchased = isFree || purchaseRepository.existsByTestIdAndStudentId(test.getId(), studentId);
            boolean maxAttemptsReached = test.getMaxAttempts() != null && attemptsUsed >= test.getMaxAttempts();
            boolean canAttempt = purchased && !maxAttemptsReached;

            List<Question> questions = questionRepository.findByTestIdOrderByOrderNoAsc(test.getId());
            int totalQuestions = questions.size();
            BigDecimal totalMarks = questions.stream()
                    .map(Question::getMarks)
                    .filter(Objects::nonNull)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new StudentTestSummaryDto(
                    test.getId(),
                    test.getTitle(),
                    test.getInstructions(),
                    test.getDurationMinutes(),
                    test.getStatus(),
                    test.getAccessType(),
                    test.getPrice(),
                    test.getMaxAttempts(),
                    attemptsUsed,
                    purchased,
                    canAttempt,
                    totalQuestions,
                    totalMarks,
                    test.getCreatedAt()
            );
        }).collect(Collectors.toList());
    }

    /**
     * POST /api/student/tests/{id}/start
     * Creates attempt, sets ends_at = now + duration (server clock),
     * enforces max_attempts and paid-purchase check; returns questions WITHOUT correct_option, marks only.
     */
    public StudentAttemptResponseDto startTest(Long testId, String studentId) {
        TestSeries test = testSeriesRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + testId));

        if (test.getStatus() != TestStatus.PUBLISHED) {
            throw new ConflictException("Test is not published and cannot be started.");
        }

        // Paid-purchase check
        if (test.getAccessType() == AccessType.PAID) {
            boolean purchased = purchaseRepository.existsByTestIdAndStudentId(test.getId(), studentId);
            if (!purchased) {
                throw new ForbiddenOperationException("This is a paid test (₹" + test.getPrice() + ") and must be purchased before attempting.");
            }
        }

        // If an active in-progress attempt already exists and has not expired, resume it
        List<Attempt> existingAttempts = attemptRepository.findByTestIdAndStudentId(test.getId(), studentId);
        Instant now = Instant.now();
        Optional<Attempt> activeAttempt = existingAttempts.stream()
                .filter(a -> a.getStatus() == AttemptStatus.IN_PROGRESS && now.isBefore(a.getEndsAt()))
                .max(Comparator.comparing(Attempt::getStartedAt));
        if (activeAttempt.isPresent()) {
            return getAttempt(activeAttempt.get().getId(), studentId);
        }

        // Max attempts check
        long attemptsCount = attemptRepository.countByTestIdAndStudentId(test.getId(), studentId);
        if (test.getMaxAttempts() != null && attemptsCount >= test.getMaxAttempts()) {
            throw new ForbiddenOperationException("Maximum attempts (" + test.getMaxAttempts() + ") exceeded for this test.");
        }

        List<Question> questions = questionRepository.findByTestIdOrderByOrderNoAsc(test.getId());

        BigDecimal totalMarks = questions.stream()
                .map(Question::getMarks)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Server clock time calculation
        Instant startedAt = Instant.now();
        Instant endsAt = startedAt.plus(Duration.ofMinutes(test.getDurationMinutes()));

        Attempt attempt = new Attempt();
        attempt.setTest(test);
        attempt.setStudentId(studentId);
        attempt.setStartedAt(startedAt);
        attempt.setEndsAt(endsAt);
        attempt.setStatus(AttemptStatus.IN_PROGRESS);
        attempt.setTotalMarks(totalMarks);
        attempt.setMarksObtained(BigDecimal.ZERO);
        attempt.setCorrect(0);
        attempt.setIncorrect(0);
        attempt.setUnanswered(questions.size());

        Attempt saved = attemptRepository.save(attempt);

        // Security requirement: questions mapped WITHOUT correct_option, marks only
        List<StudentQuestionDto> studentQuestions = questions.stream()
                .map(q -> new StudentQuestionDto(
                        q.getId(),
                        q.getOrderNo(),
                        q.getText(),
                        q.getOptionA(),
                        q.getOptionB(),
                        q.getOptionC(),
                        q.getOptionD(),
                        q.getMarks(),
                        q.getNegativeMarks()
                ))
                .collect(Collectors.toList());

        long remainingSeconds = Math.max(0, Duration.between(now, endsAt).getSeconds());

        return new StudentAttemptResponseDto(
                saved.getId(),
                test.getId(),
                test.getTitle(),
                saved.getStudentId(),
                saved.getStartedAt(),
                saved.getEndsAt(),
                remainingSeconds,
                test.getDurationMinutes(),
                saved.getStatus(),
                saved.getTotalMarks(),
                studentQuestions,
                new ArrayList<>()
        );
    }

    /**
     * PUT /api/student/attempts/{id}/answer
     * Saves selected option and marked_for_review; rejects after ends_at or if submitted.
     */
    public StudentAnswerResponseDto saveAnswer(Long attemptId, String studentId, StudentAnswerRequestDto request) {
        if (request == null || request.getQuestionId() == null) {
            throw new IllegalArgumentException("Question ID is required.");
        }

        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + attemptId));

        if (!attempt.getStudentId().equals(studentId)) {
            throw new ForbiddenOperationException("Attempt does not belong to student: " + studentId);
        }

        // Rejection rule 1: If submitted
        if (attempt.getStatus() == AttemptStatus.SUBMITTED || attempt.getSubmittedAt() != null) {
            throw new ConflictException("Attempt has already been submitted and cannot be modified.");
        }

        // Rejection rule 2: After ends_at (server clock)
        Instant now = Instant.now();
        if (now.isAfter(attempt.getEndsAt())) {
            throw new ConflictException("Attempt time limit has expired. Submission window closed at: " + attempt.getEndsAt());
        }

        Question question = questionRepository.findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + request.getQuestionId()));

        if (!question.getTest().getId().equals(attempt.getTest().getId())) {
            throw new IllegalArgumentException("Question does not belong to this test attempt.");
        }

        String selectedOption = request.getSelectedOption();
        if (selectedOption != null) {
            selectedOption = selectedOption.trim().toUpperCase();
            if (selectedOption.isEmpty()) {
                selectedOption = null;
            } else if (!Set.of("A", "B", "C", "D").contains(selectedOption)) {
                throw new IllegalArgumentException("Selected option must be one of A, B, C, D or null.");
            }
        }

        AttemptAnswerId answerId = new AttemptAnswerId(attempt.getId(), question.getId());
        AttemptAnswer answer = attemptAnswerRepository.findById(answerId)
                .orElseGet(() -> {
                    AttemptAnswer a = new AttemptAnswer();
                    a.setId(answerId);
                    a.setAttempt(attempt);
                    a.setQuestion(question);
                    return a;
                });

        answer.setSelectedOption(selectedOption);
        answer.setMarkedForReview(Boolean.TRUE.equals(request.getMarkedForReview()));
        attemptAnswerRepository.save(answer);

        long remainingSeconds = Math.max(0, Duration.between(now, attempt.getEndsAt()).getSeconds());
        return new StudentAnswerResponseDto(question.getId(), selectedOption, answer.getMarkedForReview(), remainingSeconds);
    }

    /**
     * GET /api/student/attempts/{id}
     * Resumes the attempt (answers + remaining seconds).
     */
    @Transactional(readOnly = true)
    public StudentAttemptResponseDto getAttempt(Long attemptId, String studentId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + attemptId));

        if (!attempt.getStudentId().equals(studentId)) {
            throw new ForbiddenOperationException("Attempt does not belong to student: " + studentId);
        }

        Instant now = Instant.now();
        long remainingSeconds = 0;
        if (attempt.getStatus() != AttemptStatus.SUBMITTED && now.isBefore(attempt.getEndsAt())) {
            remainingSeconds = Duration.between(now, attempt.getEndsAt()).getSeconds();
        }

        List<AttemptAnswer> answers = attemptAnswerRepository.findAnswersByAttemptId(attempt.getId());
        List<StudentAnswerDto> answerDtos = answers.stream()
                .map(a -> new StudentAnswerDto(a.getQuestion().getId(), a.getSelectedOption(), a.getMarkedForReview()))
                .collect(Collectors.toList());

        // Questions mapped WITHOUT correct_option
        List<Question> questions = questionRepository.findByTestIdOrderByOrderNoAsc(attempt.getTest().getId());
        List<StudentQuestionDto> questionDtos = questions.stream()
                .map(q -> new StudentQuestionDto(
                        q.getId(),
                        q.getOrderNo(),
                        q.getText(),
                        q.getOptionA(),
                        q.getOptionB(),
                        q.getOptionC(),
                        q.getOptionD(),
                        q.getMarks(),
                        q.getNegativeMarks()
                ))
                .collect(Collectors.toList());

        return new StudentAttemptResponseDto(
                attempt.getId(),
                attempt.getTest().getId(),
                attempt.getTest().getTitle(),
                attempt.getStudentId(),
                attempt.getStartedAt(),
                attempt.getEndsAt(),
                remainingSeconds,
                attempt.getTest().getDurationMinutes(),
                attempt.getStatus(),
                attempt.getTotalMarks(),
                questionDtos,
                answerDtos
        );
    }

    /**
     * GET /api/student/tests/{id}/active-attempt
     * Returns the active in-progress attempt (questions without correct_option, saved answers, remaining seconds)
     * or throws 404 (ResourceNotFoundException) if none exists.
     */
    @Transactional(readOnly = true)
    public StudentAttemptResponseDto getActiveAttempt(Long testId, String studentId) {
        TestSeries test = testSeriesRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + testId));

        List<Attempt> existingAttempts = attemptRepository.findByTestIdAndStudentId(test.getId(), studentId);
        Instant now = Instant.now();
        Attempt activeAttempt = existingAttempts.stream()
                .filter(a -> a.getStatus() == AttemptStatus.IN_PROGRESS && now.isBefore(a.getEndsAt()))
                .max(Comparator.comparing(Attempt::getStartedAt))
                .orElseThrow(() -> new ResourceNotFoundException("No active attempt found for test id: " + testId));

        return getAttempt(activeAttempt.getId(), studentId);
    }

    /**
     * GET /api/student/tests/{id} (PUBLISHED only)
     */
    @Transactional(readOnly = true)
    public StudentTestSummaryDto getPublishedTestById(Long testId, String studentId) {
        TestSeries test = testSeriesRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + testId));

        if (test.getStatus() != TestStatus.PUBLISHED) {
            throw new ConflictException("Test is not published.");
        }

        long attemptsUsed = attemptRepository.countByTestIdAndStudentId(test.getId(), studentId);
        boolean isFree = test.getAccessType() == AccessType.FREE;
        boolean purchased = isFree || purchaseRepository.existsByTestIdAndStudentId(test.getId(), studentId);
        boolean maxAttemptsReached = test.getMaxAttempts() != null && attemptsUsed >= test.getMaxAttempts();
        boolean canAttempt = purchased && !maxAttemptsReached;

        List<Question> questions = questionRepository.findByTestIdOrderByOrderNoAsc(test.getId());
        int totalQuestions = questions.size();
        BigDecimal totalMarks = questions.stream()
                .map(Question::getMarks)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new StudentTestSummaryDto(
                test.getId(),
                test.getTitle(),
                test.getInstructions(),
                test.getDurationMinutes(),
                test.getStatus(),
                test.getAccessType(),
                test.getPrice(),
                test.getMaxAttempts(),
                attemptsUsed,
                purchased,
                canAttempt,
                totalQuestions,
                totalMarks,
                test.getCreatedAt()
        );
    }

    /**
     * POST /api/student/attempts/{id}/submit
     * Submits the attempt, calculates score, correct, incorrect, unanswered,
     * marks_obtained, and sets status = SUBMITTED.
     * Prevents double submit idempotently.
     */
    public StudentSubmitResultDto submitAttempt(Long attemptId, String studentId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new ResourceNotFoundException("Attempt not found with id: " + attemptId));

        if (!attempt.getStudentId().equals(studentId)) {
            throw new ForbiddenOperationException("Attempt does not belong to student: " + studentId);
        }

        // Prevent double submit: If already submitted, return saved result idempotently
        if (attempt.getStatus() == AttemptStatus.SUBMITTED) {
            return toSubmitResult(attempt);
        }

        List<Question> questions = questionRepository.findByTestIdOrderByOrderNoAsc(attempt.getTest().getId());
        List<AttemptAnswer> answers = attemptAnswerRepository.findAnswersByAttemptId(attempt.getId());
        Map<Long, AttemptAnswer> answerMap = answers.stream()
                .collect(Collectors.toMap(a -> a.getQuestion().getId(), a -> a, (a1, a2) -> a1));

        int correct = 0;
        int incorrect = 0;
        int unanswered = 0;
        BigDecimal marksObtained = BigDecimal.ZERO;

        for (Question q : questions) {
            AttemptAnswer ans = answerMap.get(q.getId());
            String selected = ans != null ? ans.getSelectedOption() : null;
            if (selected == null || selected.trim().isEmpty()) {
                unanswered++;
            } else if (q.getCorrectOption() != null && selected.trim().equalsIgnoreCase(q.getCorrectOption().trim())) {
                correct++;
                if (q.getMarks() != null) {
                    marksObtained = marksObtained.add(q.getMarks());
                }
            } else {
                incorrect++;
                if (q.getNegativeMarks() != null) {
                    marksObtained = marksObtained.subtract(q.getNegativeMarks());
                }
            }
        }

        Instant submittedAt = Instant.now();
        attempt.setStatus(AttemptStatus.SUBMITTED);
        attempt.setSubmittedAt(submittedAt);
        attempt.setCorrect(correct);
        attempt.setIncorrect(incorrect);
        attempt.setUnanswered(unanswered);
        attempt.setMarksObtained(marksObtained);

        Attempt saved = attemptRepository.save(attempt);
        return toSubmitResult(saved);
    }

    private StudentSubmitResultDto toSubmitResult(Attempt attempt) {
        List<Question> questions = questionRepository.findByTestIdOrderByOrderNoAsc(attempt.getTest().getId());
        int totalQuestions = questions.size();
        BigDecimal totalMarks = attempt.getTotalMarks() != null ? attempt.getTotalMarks() : BigDecimal.ZERO;
        BigDecimal marksObtained = attempt.getMarksObtained() != null ? attempt.getMarksObtained() : BigDecimal.ZERO;

        Double percentage = 0.0;
        if (totalMarks.compareTo(BigDecimal.ZERO) > 0) {
            percentage = marksObtained.doubleValue() / totalMarks.doubleValue() * 100.0;
            // Round to 2 decimal places
            percentage = Math.round(percentage * 100.0) / 100.0;
        }

        return new StudentSubmitResultDto(
                attempt.getId(),
                attempt.getTest().getId(),
                attempt.getTest().getTitle(),
                attempt.getStudentId(),
                attempt.getStartedAt(),
                attempt.getSubmittedAt(),
                attempt.getStatus(),
                totalMarks,
                marksObtained,
                totalQuestions,
                attempt.getCorrect(),
                attempt.getIncorrect(),
                attempt.getUnanswered(),
                percentage
        );
    }
}
