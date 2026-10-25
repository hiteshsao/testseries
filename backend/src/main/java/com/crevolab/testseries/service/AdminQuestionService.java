package com.crevolab.testseries.service;

import com.crevolab.testseries.domain.Question;
import com.crevolab.testseries.domain.TestSeries;
import com.crevolab.testseries.dto.admin.QuestionRequestDto;
import com.crevolab.testseries.dto.admin.QuestionResponseDto;
import com.crevolab.testseries.exception.ResourceNotFoundException;
import com.crevolab.testseries.repository.QuestionRepository;
import com.crevolab.testseries.repository.TestSeriesRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminQuestionService {

    private final QuestionRepository questionRepository;
    private final TestSeriesRepository testSeriesRepository;

    public AdminQuestionService(QuestionRepository questionRepository, TestSeriesRepository testSeriesRepository) {
        this.questionRepository = questionRepository;
        this.testSeriesRepository = testSeriesRepository;
    }

    @Transactional(readOnly = true)
    public List<QuestionResponseDto> getQuestionsByTestId(Long testId) {
        if (!testSeriesRepository.existsById(testId)) {
            throw new ResourceNotFoundException("Test not found with id: " + testId);
        }
        return questionRepository.findByTestIdOrderByOrderNoAsc(testId)
                .stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuestionResponseDto getQuestionById(Long questionId) {
        Question q = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));
        return toResponseDto(q);
    }

    public QuestionResponseDto createQuestion(Long testId, QuestionRequestDto request) {
        TestSeries test = testSeriesRepository.findById(testId)
                .orElseThrow(() -> new ResourceNotFoundException("Test not found with id: " + testId));

        // New manual questions append at the end
        Integer maxOrderNo = questionRepository.findMaxOrderNoByTestId(test.getId());
        int nextOrderNo = (maxOrderNo != null ? maxOrderNo : 0) + 1;

        Question question = new Question();
        question.setTest(test);
        question.setOrderNo(nextOrderNo);
        question.setText(request.getText());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setCorrectOption(normalizeCorrectOption(request.getCorrectOption()));
        question.setMarks(request.getMarks());
        question.setNegativeMarks(request.getNegativeMarks());

        Question saved = questionRepository.save(question);
        return toResponseDto(saved);
    }

    public QuestionResponseDto updateQuestion(Long questionId, QuestionRequestDto request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        question.setText(request.getText());
        question.setOptionA(request.getOptionA());
        question.setOptionB(request.getOptionB());
        question.setOptionC(request.getOptionC());
        question.setOptionD(request.getOptionD());
        question.setCorrectOption(normalizeCorrectOption(request.getCorrectOption()));
        question.setMarks(request.getMarks());
        question.setNegativeMarks(request.getNegativeMarks());

        Question updated = questionRepository.save(question);
        return toResponseDto(updated);
    }

    public void deleteQuestion(Long questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException("Question not found with id: " + questionId));

        Long testId = question.getTest().getId();

        questionRepository.delete(question);
        questionRepository.flush();

        // After DELETE re-number order_no so there are no gaps
        List<Question> remaining = questionRepository.findByTestIdOrderByOrderNoAsc(testId);
        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setOrderNo(i + 1);
        }
        questionRepository.saveAll(remaining);
    }

    private String normalizeCorrectOption(String option) {
        if (option == null || option.isBlank()) {
            return null;
        }
        String trimmed = option.trim().toUpperCase();
        if (!trimmed.matches("^[A-D]$")) {
            throw new IllegalArgumentException("Correct option must be A, B, C, D or null");
        }
        return trimmed;
    }

    private QuestionResponseDto toResponseDto(Question q) {
        return new QuestionResponseDto(
                q.getId(),
                q.getTest().getId(),
                q.getOrderNo(),
                q.getText(),
                q.getOptionA(),
                q.getOptionB(),
                q.getOptionC(),
                q.getOptionD(),
                q.getCorrectOption(),
                q.getMarks(),
                q.getNegativeMarks()
        );
    }
}
